/**
 * Copyright (c) 2016 - 2018 Syncleus, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * This material was prepared as an account of work sponsored by an agency of the United States Government.
 * Neither the United States Government nor the United States Department of Energy, nor Battelle, nor any of
 * their employees, nor any jurisdiction or organization that has cooperated in the development of these materials,
 * makes any warranty, express or implied, or assumes any legal liability or responsibility for the accuracy,
 * completeness, or usefulness or any information, apparatus, product, software, or process disclosed, or represents
 * that its use would not infringe privately owned rights.
 * <p>
 * This material was prepared as an account of work sponsored by an agency of the United States Government.
 * Neither the United States Government nor the United States Department of Energy, nor Battelle, nor any of
 * their employees, nor any jurisdiction or organization that has cooperated in the development of these materials,
 * makes any warranty, express or implied, or assumes any legal liability or responsibility for the accuracy,
 * completeness, or usefulness or any information, apparatus, product, software, or process disclosed, or represents
 * that its use would not infringe privately owned rights.
 * <p>
 * This material was prepared as an account of work sponsored by an agency of the United States Government.
 * Neither the United States Government nor the United States Department of Energy, nor Battelle, nor any of
 * their employees, nor any jurisdiction or organization that has cooperated in the development of these materials,
 * makes any warranty, express or implied, or assumes any legal liability or responsibility for the accuracy,
 * completeness, or usefulness or any information, apparatus, product, software, or process disclosed, or represents
 * that its use would not infringe privately owned rights.
 * <p>
 * This material was prepared as an account of work sponsored by an agency of the United States Government.
 * Neither the United States Government nor the United States Department of Energy, nor Battelle, nor any of
 * their employees, nor any jurisdiction or organization that has cooperated in the development of these materials,
 * makes any warranty, express or implied, or assumes any legal liability or responsibility for the accuracy,
 * completeness, or usefulness or any information, apparatus, product, software, or process disclosed, or represents
 * that its use would not infringe privately owned rights.
 * <p>
 * This material was prepared as an account of work sponsored by an agency of the United States Government.
 * Neither the United States Government nor the United States Department of Energy, nor Battelle, nor any of
 * their employees, nor any jurisdiction or organization that has cooperated in the development of these materials,
 * makes any warranty, express or implied, or assumes any legal liability or responsibility for the accuracy,
 * completeness, or usefulness or any information, apparatus, product, software, or process disclosed, or represents
 * that its use would not infringe privately owned rights.
 */
/**
 * This material was prepared as an account of work sponsored by an agency of the United States Government.  
 * Neither the United States Government nor the United States Department of Energy, nor Battelle, nor any of 
 * their employees, nor any jurisdiction or organization that has cooperated in the development of these materials, 
 * makes any warranty, express or implied, or assumes any legal liability or responsibility for the accuracy, 
 * completeness, or usefulness or any information, apparatus, product, software, or process disclosed, or represents
 * that its use would not infringe privately owned rights.
 */
package com.oneliang.ktx.frame.test.ai.aparapi.matrix;

import com.aparapi.Kernel;
import com.aparapi.Kernel.EXECUTION_MODE;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.OpenCLDevice;
import com.oneliang.ktx.util.logging.Logger;
import com.oneliang.ktx.util.logging.LoggerManager;
import kotlin.jvm.internal.ClassReference;

/**
 * GPU calculations using OpenBitSet Intersection for OpenBitSets
 *
 * Based on code from:
 * <a href="http://grepcode.com/file/repo1.maven.org/maven2/org.apache.lucene/lucene-core/3.1.0/org/apache/lucene/util/BitUtil.java">apache.lucene.util.BitUtil.java</a>
 *
 * @author ryan.lamothe at gmail.com
 * @author sedillard at gmail.com
 */
public class CorrMatrixHost {

    private static final Logger logger = LoggerManager.INSTANCE.getLogger(new ClassReference(CorrMatrixHost.class));

    /**
     * Perform matrix intersection for two lists of Lucene OpenBitSet-based packed longs
     *
     * @param matrixA
     *    The first term-document matrix
     * @param matrixB
     *    The second term-document matrix
     * @param deviceType Device.TYPE
     * @return result Matrix
     */
    public static long[][] intersectionMatrix(final long[][] matrixA, final long[][] matrixB, final Device.TYPE deviceType) {

        // Basic validation
        if (matrixA == null) {
            throw new NullPointerException("MatrixA cannot be NULL");
        }
        if (matrixB == null) {
            throw new NullPointerException("MatrixB cannot be NULL");
        }

        // Size of an array is 8 bytes for the object + 4 bytes for the header and length information
        final int arrayMemOverhead = 12;

        // numDocs/64 since they are packed into longs
        // We need to make our matrix sizes multiples of BLOCK_SIZE
        final int matrixA_numTerms = matrixA.length;
        final int matrixA_numLongs = matrixA[0].length;

//      if (logger.isDebugEnabled()) {
        logger.debug("----------");
        logger.debug("MatrixA NumTerms (Rows): " + matrixA_numTerms);
        logger.debug("MatrixA NumLongs (Columns): " + matrixA_numLongs);
        logger.debug("MatrixA NumDocs: " + (matrixA_numLongs * 64L));
//      }

        final long matrixA_BytesPerRow = matrixA_numLongs * 8L;
        final long matrixA_TotalBytes = (matrixA_numTerms * matrixA_BytesPerRow) + arrayMemOverhead;

//      if (logger.isDebugEnabled()) {
        logger.debug("MatrixA Total Memory Size: " + humanReadableByteCount(matrixA_TotalBytes, true));
//      }

        final int matrixB_numTerms = matrixB.length;
        final int matrixB_numLongs = matrixB[0].length;

//      if (logger.isDebugEnabled()) {
        logger.debug("----------");
        logger.debug("MatrixB NumTerms (Rows): " + matrixB_numTerms);
        logger.debug("MatrixB NumLongs (Columns): " + matrixB_numLongs);
        logger.debug("MatrixB NumDocs: " + (matrixB_numLongs * 64L));
//      }

        final long matrixB_BytesPerRow = matrixB_numLongs * 8L;
        final long matrixB_TotalBytes = (matrixB_numTerms * matrixB_BytesPerRow) + arrayMemOverhead;

//      if (logger.isDebugEnabled()) {
        logger.debug("MatrixB Total Memory Size: " + humanReadableByteCount(matrixB_TotalBytes, true));
        logger.debug("----------");
//      }

        final long[][] resultMatrix = new long[matrixA_numTerms][matrixB_numTerms];

//      if (logger.isDebugEnabled()) {
        final long resultMatrix_TotalBytes = (matrixA_numTerms * matrixB_numTerms * 4L) + arrayMemOverhead;
        logger.debug("ResultMatrix Memory Size: " + humanReadableByteCount(resultMatrix_TotalBytes, true));
        logger.debug("Total Requested Memory Size: " + humanReadableByteCount(matrixA_TotalBytes + matrixB_TotalBytes + resultMatrix_TotalBytes, true));
        logger.debug("----------");
//      }

        int NUM_SUB_ROWS = matrixA_numTerms; // Default number of sub-rows

        OpenCLDevice device = null;

        // We do not test for EXECUTION_MODE.JTP because JTP is non-OpenCL

        if (deviceType.equals(Device.TYPE.CPU)) {
            device = (OpenCLDevice) Device.firstCPU();

            if (device == null) {
                logger.warning("OpenCLDevice.CPU is NULL...OpenCL is unavailable. Setting to JTP mode.");
                logger.debug("----------");
            }
        } else if (deviceType.equals(Device.TYPE.GPU)) {
            device = (OpenCLDevice) Device.best();

            if (device == null) {
                logger.warning("OpenCLDevice.GPU is NULL...OpenCL is unavailable. Setting to JTP mode.");
                logger.debug("----------");
            }
        }

        // This is to create stripes of rows that will fit into OpenCL's available memory
        // Calculate the number of sub-rows by calling OpenCL to find out available memory
        // Length of row * 8 (size of long in bytes) * number of rows to available memory
        final int maxNumTerms = Math.max(matrixA_numTerms, matrixB_numTerms);


        if (device != null) {
            final long globalMemSize = device.getGlobalMemSize();
            // final long maxMemAllocSize = Math.max((globalMemSize/4), 128*1024*1024);
            final long maxMemAllocSize = device.getMaxMemAllocSize();

            // 1048576 bytes in a megabyte (1024*1024)
            // Java long is 8 bytes
            // 131072 longs in 1 megabyte
            // SAFE OpenCL spec allocation is max(1/4 GlobalMemSize)
            // ***During our testing this appears to be incorrectly/inconsistently reported depending on os/drivers/hardware***
//         if (logger.isDebugEnabled()) {
            logger.debug("Available OpenCL globalMemSize: " + humanReadableByteCount(globalMemSize, true));
            logger.debug("Available OpenCL maxMemAllocSize: " + humanReadableByteCount(maxMemAllocSize, true));
//         }

            // Maybe there is a more clever way to do this :)
            // The idea here is to decide how many sub-rows of the matrix we can fit on a single card
            // The long-term goal to divide up the work for both small RAM GPUs and multiple GPUs
            int subRowsCounterA = 0;
            int subRowsCounterB = 0;
            long subRowsMemSizeA = 0L;
            long subRowsMemSizeB = 0L;
            long subResultMatrixMemSize = 0L;
            long subTotalMemSize = 0L;

            do {
                if (subRowsCounterA < matrixA_numTerms) {
                    subRowsMemSizeA = subRowsCounterA != 0 ? (subRowsCounterA * matrixA_numLongs * 8L) + arrayMemOverhead : 0;
                    subRowsCounterA += 1;
                } else if (subRowsCounterA == matrixA_numTerms) {
                    subRowsMemSizeA = subRowsCounterA != 0 ? (subRowsCounterA * matrixA_numLongs * 8L) + arrayMemOverhead : 0;
                }

                if (subRowsCounterB < matrixB_numTerms) {
                    subRowsMemSizeB = subRowsCounterB != 0 ? (subRowsCounterB * matrixB_numLongs * 8L) + arrayMemOverhead : 0;
                    subRowsCounterB += 1;
                } else if (subRowsCounterB == matrixB_numTerms) {
                    subRowsMemSizeB = subRowsCounterB != 0 ? (subRowsCounterB * matrixB_numLongs * 8L) + arrayMemOverhead : 0;
                }

                // This is 4 bytes since the sub-result matrix is an int array
                subResultMatrixMemSize = ((subRowsCounterA * subRowsCounterB) * 4L) + arrayMemOverhead;

                subTotalMemSize = subRowsMemSizeA + subRowsMemSizeB + subResultMatrixMemSize;
            } while ((Math.max(subRowsCounterA, subRowsCounterB) < maxNumTerms) && (subTotalMemSize <= maxMemAllocSize));

            // If using OpenCL override the default number of subrows
            NUM_SUB_ROWS = Math.max(subRowsCounterA, subRowsCounterB);

            if (NUM_SUB_ROWS < maxNumTerms) {
                final long subMatrixA_memSize = (NUM_SUB_ROWS * matrixA_numLongs * 8L) + arrayMemOverhead;
                final long subMatrixB_memSize = (NUM_SUB_ROWS * matrixB_numLongs * 8L) + arrayMemOverhead;
                final long subResultMatrix_memSize = (NUM_SUB_ROWS * NUM_SUB_ROWS * 4L) + arrayMemOverhead;

                logger.warning("****************************************************************");
                logger.warning("Requested matrix computation is larger than available OpenCL memory");
                logger.warning("Matrix striping is occurring to fit all data into OpenCL memory...");
                logger.warning("");
                logger.warning("Number rows requested: " + maxNumTerms);
                logger.warning("Number rows that fit: " + NUM_SUB_ROWS);
                logger.warning("");
                logger.warning("SubMatrixA Memory Size: " + humanReadableByteCount(subMatrixA_memSize, true));
                logger.warning("SubMatrixB Memory Size: " + humanReadableByteCount(subMatrixB_memSize, true));
                logger.warning("SubResultMatrix Memory Size: " + humanReadableByteCount(subResultMatrix_memSize, true));
                logger.warning("SubMatrix Total Memory Size: " + humanReadableByteCount(subMatrixA_memSize + subMatrixB_memSize + subResultMatrix_memSize, true));
                logger.warning("****************************************************************");
            }
        }

        final int numSubBlocksA = ((matrixA_numTerms + NUM_SUB_ROWS) - 1) / NUM_SUB_ROWS;
        final int numSubBlocksB = ((matrixB_numTerms + NUM_SUB_ROWS) - 1) / NUM_SUB_ROWS;

        final long[] subMatrixA = new long[NUM_SUB_ROWS * matrixA_numLongs];
        final long[] subMatrixB = new long[NUM_SUB_ROWS * matrixB_numLongs];
        final long[] subResultMatrix = new long[NUM_SUB_ROWS * NUM_SUB_ROWS];

        final CorrMatrixKernel kernel = new CorrMatrixKernel(subMatrixA, NUM_SUB_ROWS, subMatrixB, NUM_SUB_ROWS, matrixA_numLongs, subResultMatrix);
        kernel.setExplicit(true);

        // Here we define a fall-back strategy, since the user may have wanted to execute only a single execution mode
        if (deviceType.equals(Device.TYPE.GPU) && (device != null)) {
            kernel.addExecutionModes(EXECUTION_MODE.GPU, EXECUTION_MODE.CPU, EXECUTION_MODE.JTP);
            logger.debug("Execution Fallback Strategy: GPU --> CPU --> JTP");
        } else if (deviceType.equals(Device.TYPE.CPU) && (device != null)) {
            kernel.addExecutionModes(EXECUTION_MODE.CPU, EXECUTION_MODE.JTP);
            logger.debug("Execution Fallback Strategy: CPU --> JTP");
        } else {
            kernel.addExecutionModes(EXECUTION_MODE.JTP);
            logger.debug("Execution Strategy: JTP");
        }

        try {
            for (int a = 0; a < numSubBlocksA; a++) {
                for (int b = 0; b < numSubBlocksB; b++) {
                    final int aSubRowStart = a * NUM_SUB_ROWS;
                    final int aSubRowEnd = Math.min(matrixA_numTerms, aSubRowStart + NUM_SUB_ROWS);

                    for (int i = aSubRowStart; i < aSubRowEnd; i++) {
                        if (matrixA_numLongs != matrixA[i].length) {
                            throw new IllegalStateException("All rows in the matrix need be the same length");
                        }

                        System.arraycopy(matrixA[i], 0, subMatrixA, (i - aSubRowStart) * matrixA_numLongs, matrixA_numLongs);
                    }

                    final int bSubRowStart = b * NUM_SUB_ROWS;
                    final int bSubRowEnd = Math.min(matrixB_numTerms, bSubRowStart + NUM_SUB_ROWS);

                    for (int i = bSubRowStart; i < bSubRowEnd; i++) {
                        if (matrixA_numLongs != matrixB[i].length) {
                            throw new IllegalStateException("All rows in the matrix need be the same length");
                        }

                        System.arraycopy(matrixB[i], 0, subMatrixB, (i - bSubRowStart) * matrixB_numLongs, matrixB_numLongs);
                    }

                    // Since matrixA_NumLongs == matrixB_NumLongs we're only going to pass matrixA_NumLongs
                    executeKernel(device, subMatrixA, aSubRowEnd - aSubRowStart, subMatrixB, bSubRowEnd - bSubRowStart, matrixA_numLongs, subResultMatrix, kernel);

                    // Convert one dimensional array to two dimensional array in the expected output ordering
                    for (int i = 0; i < NUM_SUB_ROWS; i++) {
                        if ((i + aSubRowStart) < aSubRowEnd) {
                            System.arraycopy(subResultMatrix, i * NUM_SUB_ROWS, resultMatrix[i + aSubRowStart], bSubRowStart, bSubRowEnd - bSubRowStart);
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
//         if (logger.isDebugEnabled()) {
            logger.debug("----------");
            logger.debug("Aparapi Gross Execution Time: " + kernel.getAccumulatedExecutionTime() + " ms <------ Aparapi");
            logger.debug("OpenCL Generation Time: " + kernel.getConversionTime() + " ms");
            logger.debug("Kernel Net Execution Time: " + (kernel.getAccumulatedExecutionTime() - kernel.getConversionTime()) + " ms");
            logger.debug("----------");
//         }

            try {
                kernel.dispose();
            } catch (final UnsatisfiedLinkError e) {
                logger.error("Aparapi failed to dispose of the kernel", e);
            }
        }

        return resultMatrix;
    }

    /**
     * Execute the GPU kernel
     *
     * @param subMatrixA
     * @param matrixA_NumTerms
     * @param subMatrixB
     * @param matrixB_NumTerms
     * @param numLongs
     * @param subResultMatrix
     * @param kernel
     *
     * @return resultMatrix
     */
    private static void executeKernel(final Device device, final long[] subMatrixA, final int matrixA_NumTerms, final long[] subMatrixB, final int matrixB_NumTerms, final int numLongs, final long[] subResultMatrix, final Kernel kernel) {

        // Power of Two for best performance
        int matrixA_NumTermsRnd = matrixA_NumTerms;
        while (!isPowerOfTwo(matrixA_NumTermsRnd)) {
            matrixA_NumTermsRnd += 1;
        }

        int matrixB_NumTermsRnd = matrixB_NumTerms;
        while (!isPowerOfTwo(matrixB_NumTermsRnd)) {
            matrixB_NumTermsRnd += 1;
        }

        final Range range;
        if (device != null) {
            range = Range.create2D(device, matrixA_NumTermsRnd, matrixB_NumTermsRnd);
        } else {
            range = Range.create2D(matrixA_NumTermsRnd, matrixB_NumTermsRnd);
        }

//      if (logger.isDebugEnabled()) {
        logger.debug("device: " + device);
        logger.debug("Range: " + range);
//      }

        kernel.put(subMatrixA);
        kernel.put(subMatrixB);
        kernel.put(subResultMatrix);

        kernel.execute(range);

        kernel.get(subResultMatrix);
    }

    /**
     * Highly efficient means to compute whether a number is a power of 2<br>
     * Based on code from http://graphics.stanford.edu/~seander/bithacks.html#DetermineIfPowerOf2
     * <p>
     * Another very cool way to do this is ((x&(-x))==x)
     *
     * @param n
     * @return boolean
     */
    private static boolean isPowerOfTwo(int n) {
        return (n > 0) && ((n & (n - 1)) == 0);
    }

    /**
     * Rounds a number to the multiple indicated
     *
     * @param num
     * @param multiple
     * @return
     */
    private static int roundToMultiple(double num, int multiple) {
        return (int) (Math.ceil(num / multiple) * multiple);
    }

    /**
     * Very nice means to convert byte sizes into human readable format<br>
     * Based on code from http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
     * <p>
     *
     * @param bytes
     * @param si
     * @return humanReadableByteCount
     */
    private static String humanReadableByteCount(long bytes, boolean si) {
        final int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(unit));
        final String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");

        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
