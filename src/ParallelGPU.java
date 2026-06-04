import java.io.*;
import java.nio.file.*;
import org.jocl.*;
import static org.jocl.CL.*;

public class ParallelGPU {

    public static Resultado contar(String caminhoArquivo, String palavra) throws Exception {
        String nomeArquivo = Paths.get(caminhoArquivo).getFileName().toString();
        String texto = new String(Files.readAllBytes(Paths.get(caminhoArquivo)));
        String textoLimpo = texto.toLowerCase().replaceAll("[^a-zà-ÿ ]", " ").replaceAll("\\s+", " ").trim();
        String textoNulo = textoLimpo + "\0";
        byte[] textoBytes = textoNulo.getBytes();
        byte[] palavraBytes = palavra.getBytes();
        int textLen = textoBytes.length;
        int wordLen = palavraBytes.length;
        int numWorkers = 256;

        CL.setExceptionsEnabled(true);

        cl_platform_id[] platforms = new cl_platform_id[1];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[0];

        cl_device_id[] devices = new cl_device_id[1];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, devices.length, devices, null);
        cl_device_id device = devices[0];

        cl_context context = clCreateContext(null, 1, new cl_device_id[]{device}, null, null, null);
        cl_command_queue queue = clCreateCommandQueue(context, device, 0, null);

        String kernelSource =
            "__kernel void count_word(" +
            "   __global const char* text," +
            "   __global const char* word," +
            "   const int textLen," +
            "   const int wordLen," +
            "   __global int* results" +
            ") {" +
            "   int gid = get_global_id(0);" +
            "   int numWorkers = get_global_size(0);" +
            "   int chunkSize = (textLen + numWorkers - 1) / numWorkers;" +
            "   int start = gid * chunkSize;" +
            "   int end = min(start + chunkSize, textLen);" +
            "   int count = 0;" +
            "   for (int i = start; i < end; i++) {" +
            "       if (i + wordLen > textLen) break;" +
            "       int isWordStart = (i == 0 || text[i-1] == ' ');" +
            "       if (!isWordStart) continue;" +
            "       int match = 1;" +
            "       for (int j = 0; j < wordLen; j++) {" +
            "           if (text[i + j] != word[j]) { match = 0; break; }" +
            "       }" +
            "       if (match) {" +
            "           int wordEnd = (i + wordLen >= textLen) || " +
            "               text[i + wordLen] == ' ' || text[i + wordLen] == '\\0';" +
            "           if (wordEnd) count++;" +
            "       }" +
            "   }" +
            "   results[gid] = count;" +
            "}";

        cl_program program = clCreateProgramWithSource(context, 1, new String[]{kernelSource}, null, null);
        clBuildProgram(program, 0, null, null, null, null);
        cl_kernel kernel = clCreateKernel(program, "count_word", null);

        cl_mem textBuf = clCreateBuffer(context, CL_MEM_READ_ONLY, (long)Sizeof.cl_char * textLen, null, null);
        cl_mem wordBuf = clCreateBuffer(context, CL_MEM_READ_ONLY, (long)Sizeof.cl_char * wordLen, null, null);
        cl_mem resultBuf = clCreateBuffer(context, CL_MEM_WRITE_ONLY, (long)Sizeof.cl_int * numWorkers, null, null);

        long inicio = System.nanoTime();

        clEnqueueWriteBuffer(queue, textBuf, CL_TRUE, 0, (long)textoBytes.length, Pointer.to(textoBytes), 0, null, null);
        clEnqueueWriteBuffer(queue, wordBuf, CL_TRUE, 0, (long)palavraBytes.length, Pointer.to(palavraBytes), 0, null, null);

        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(textBuf));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(wordBuf));
        clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(new int[]{textLen}));
        clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{wordLen}));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(resultBuf));

        long[] globalWorkSize = new long[]{numWorkers};
        clEnqueueNDRangeKernel(queue, kernel, 1, null, globalWorkSize, null, 0, null, null);
        clFinish(queue);

        int[] results = new int[numWorkers];
        clEnqueueReadBuffer(queue, resultBuf, CL_TRUE, 0, (long)Sizeof.cl_int * numWorkers, Pointer.to(results), 0, null, null);

        int count = 0;
        for (int i = 0; i < numWorkers; i++) {
            count += results[i];
        }

        long fim = System.nanoTime();
        long tempoMs = (fim - inicio) / 1_000_000;

        clReleaseMemObject(textBuf);
        clReleaseMemObject(wordBuf);
        clReleaseMemObject(resultBuf);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(queue);
        clReleaseContext(context);

        return new Resultado("ParallelGPU", nomeArquivo, palavra, count, tempoMs, 0);
    }
}
