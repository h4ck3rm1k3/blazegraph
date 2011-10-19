/**

Copyright (C) SYSTAP, LLC 2006-2011.  All rights reserved.

Contact:
     SYSTAP, LLC
     4501 Tower Road
     Greensboro, NC 27410
     licenses@bigdata.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
/*
 * Created on Oct 19, 2011
 */

package com.bigdata.htree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.bigdata.btree.BTreeCounters;
import com.bigdata.btree.DefaultTupleSerializer;
import com.bigdata.btree.ITupleSerializer;
import com.bigdata.btree.IndexMetadata;
import com.bigdata.btree.keys.ASCIIKeyBuilderFactory;
import com.bigdata.btree.keys.IKeyBuilder;
import com.bigdata.btree.keys.KeyBuilder;
import com.bigdata.btree.raba.codec.SimpleRabaCoder;
import com.bigdata.io.DirectBufferPool;
import com.bigdata.rawstore.Bytes;
import com.bigdata.rawstore.IRawStore;
import com.bigdata.rwstore.sector.IMemoryManager;
import com.bigdata.rwstore.sector.MemStore;
import com.bigdata.rwstore.sector.MemoryManager;

/**
 * A simple demonstration which may be used to compare the {@link HTree}
 * performance against a Java collections class such as {@link HashMap} or
 * {@link LinkedHashMap}. This demonstration is focused on the performance curve
 * when inserting a large number of keys into a collection. The Java collection
 * classes are faster for small numbers of keys, but the {@link HTree} rapidly
 * out performs them as the #of keys grows larger.
 * <p>
 * The {@link HTree} is run against the {@link MemoryManager}. This means that
 * the data for the {@link HTree} is mostly stored on the Java native process
 * heap using {@link java.nio.ByteBuffer#allocateDirect(int)}. Thus even a very
 * large {@link HTree} instance can be run with a very small JVM object heap and
 * creates nearly no GC pressure.
 * <p>
 * The {@link HTree} permits multiple entries for the same key. While variable
 * length keys are supported, the key for the {@link HTree} is typically a 32
 * (or 64) bit hash code for the value stored in the {@link HTree}. Normally,
 * the application will lookup all tuples having the same hash code and then
 * enforce {@link Set} or {@link Map} semantics by scanning those tuples for the
 * presence of the same application object.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @author <a href="mailto:martyncutcher@users.sourceforge.net">Martyn
 *         Cutcher</a>
 * @version $Id$
 */
public class HTreeVersusHashMapDemo {

    private static final Logger log = Logger
            .getLogger(HTreeVersusHashMapDemo.class);

    /**
     * Provision and return an {@link HTree} instance.
     * 
     * @param store
     *            The backing store.
     * @param addressBits
     *            The #of address bits (10 is typical and gives you directory
     *            pages with 2^10 slots, which is a 1024 fan-out).
     * @param rawRecords
     *            <code>true</code> iff raw record support will be enabled.
     * @param writeRetentionQueueCapacity
     *            The write retention queue capacity controls how long mutable
     *            htree nodes will be buffered on the JVM heap before being
     *            incrementally evicted to the backing store.
     * 
     * @return The {@link HTree} instance.
     */
    static private HTree getHTree(final IRawStore store, final int addressBits,
            final boolean rawRecords, final int writeRetentionQueueCapacity) {

        final ITupleSerializer<?, ?> tupleSer = new DefaultTupleSerializer(
                new ASCIIKeyBuilderFactory(Bytes.SIZEOF_INT),
                // new FrontCodedRabaCoder(),// TODO FrontCodedRaba
                new SimpleRabaCoder(),// keys
                new SimpleRabaCoder() // vals
        );

        final IndexMetadata metadata = new IndexMetadata(UUID.randomUUID());

        if (rawRecords) {
            metadata.setRawRecords(true);
            metadata.setMaxRecLen(0);
        }

        metadata.setAddressBits(addressBits);

        metadata.setTupleSerializer(tupleSer);

        /*
         * Note: A low retention queue capacity will drive evictions, which is
         * good from the perspective of stressing the persistence store
         * integration.
         */
        metadata.setWriteRetentionQueueCapacity(writeRetentionQueueCapacity);
        metadata.setWriteRetentionQueueScan(10); // Must be LTE capacity.

        return HTree.create(store, metadata);

    }

    static final int REPORT_INTERVAL = 10000; // report interval.

    /**
     * Interface for reporting on progress.
     */
    interface IReport {

        /**
         * Report hook.
         * 
         * @param nops
         *            The #of operations performed.
         * @param elapsed
         *            The elapsed time in milliseconds.
         * @param store
         *            The backing store for the {@link HTree}.
         */
        void report(long nops, long elapsed, final IMemoryManager mmgr,
                IRawStore store);
        
    }

    private static class ReportListener implements IReport {

        public ReportListener() {
            System.out
                    .println("inserts\telapsed(ms)\tinserts/sec\tfreeMemory\ttotalMemory\tuserBytes\tmmgrBytes");
        }

        @Override
        public void report(long nops, long elapsed, final IMemoryManager mmgr,
                final IRawStore store) {

            final long insertsPerSec = (long) (((double) nops) / (elapsed / 1000d));

            final long freeMemory = Runtime.getRuntime().freeMemory();

            final long totalMemory = Runtime.getRuntime().totalMemory();

            // #of application bytes in the store.
            final long userBytes = store.size();

            // total extent of the store (1M increments).
            final long mmgrBytes = ((MemoryManager) mmgr).getExtent();

            System.out.println(nops + "\t" + elapsed + "\t" + insertsPerSec
                    + "\t" + freeMemory + "\t" + totalMemory + "\t" + userBytes
                    + "\t" + mmgrBytes);

        }

    }
    
    private static class HTreeDemo implements Runnable {

        private final int nkeys;
        private final int vectorSize;
        private final long seed;
        private final int addressBits;
        private final int writeRetentionQueueCapacity;
        private final IReport report;

        /**
         * 
         * @param nkeys
         *            The #of keys to insert.
         * @param vectorSize
         *            The #of keys which are sorted (vectored) to improve IO
         *            efficiency.
         * @param seed
         *            The random generator seed.
         * @param addressBits
         *            The address bits for the {@link HTree}.
         * @param writeRetentionQueueCapacity
         *            The capacity of the write retention queue.
         */
        HTreeDemo(IReport report, final int nkeys, final int vectorSize,
                final long seed, final int addressBits,
                final int writeRetentionQueueCapacity) {

            this.report = report;
            this.nkeys = nkeys;
            this.vectorSize = vectorSize;
            this.seed = seed;
            this.addressBits = addressBits;
            this.writeRetentionQueueCapacity = writeRetentionQueueCapacity;

        }
        
        public void run() {

            final long start = System.currentTimeMillis();

            final MemStore store = new MemStore(DirectBufferPool.INSTANCE,
                    Integer.MAX_VALUE);

            try {

                final HTree htree = getHTree(store, addressBits,
                        false/* rawRecords */, writeRetentionQueueCapacity);

                final Random r = new Random(seed);
                
                final IKeyBuilder keyBuilder = new KeyBuilder();

                final byte[] val = null; // no value stored under the key.
                
                for (int i = 0; i < nkeys; i += vectorSize) {

                    final int[] keys = new int[vectorSize];

                    for (int j = 0; j < vectorSize; j++) {

                        final int rnd = r.nextInt();
                        
                        keys[j] = rnd;

                    }
                    
                    // Vector the chunk.
                    Arrays.sort(keys);

                    for (int j = 0; j < vectorSize; j++) {

                        final int rnd = keys[j];

                        final byte[] key = keyBuilder.reset().append(rnd)
                                .getKey();

                        if (!htree.contains(key)) {
                            /*
                             * Do not store duplicate entries since we will
                             * compare the performance to a Set.
                             */
                            htree.insert(key, val);
                        }

                        final long nops = i + j;

                        if (report != null && (nops % REPORT_INTERVAL) == 0L) {

                            final long elapsed = System.currentTimeMillis()
                                    - start;

                            report.report(nops, elapsed,
                                    store.getMemoryManager(), store);

                        }

                    }
                    
                }

                final long load = System.currentTimeMillis();
                
                final BTreeCounters counters = htree.getBtreeCounters();
                
                if (log.isInfoEnabled()) {

                    log.info("Htree Leaves: " + htree.nleaves + ", Evicted: "
                            + counters.leavesWritten + ", Nodes: " + htree.nnodes
                            + ", Evicted: " + counters.nodesWritten);

                    log.info("Load took " + (load - start) + "ms");
                    
                }

//                htree.writeCheckpoint();
                
            } finally {

                store.destroy();

            }
        }
        
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {

        final int nkeys = 2 * Bytes.megabyte32;
        final int vectorSize = 10000;
        final long seed = -91L;
        final int addressBits = 10; // pages with 2^10 slots.
        final int writeRetentionQueueCapacity = 500;

        new HTreeDemo(new ReportListener(), nkeys, vectorSize, seed, addressBits,
                writeRetentionQueueCapacity).run();

    }

}