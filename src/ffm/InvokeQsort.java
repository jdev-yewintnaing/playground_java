package ffm;

import java.lang.foreign.*;
import java.lang.invoke.*;

public class InvokeQsort {
        
    class Qsort {
        static int qsortCompare(MemorySegment elem1, MemorySegment elem2) {
            return Integer.compare(elem1.get(ValueLayout.JAVA_INT, 0), elem2.get(ValueLayout.JAVA_INT, 0));
        }
    }

    // native code
//    void qsort(void *base, size_t nmemb, size_t size,
//               int (*compar)(const void *, const void *));


    // Obtain instance of native linker
    final static Linker linker = Linker.nativeLinker();
    
    static int[] qsortTest(int[] unsortedArray) throws Throwable {
        
        int[] sorted = null;
        
        // Create downcall handle for qsort
        MethodHandle qsort = linker.downcallHandle(
            linker.defaultLookup().find("qsort").get(),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS,
                                      ValueLayout.JAVA_LONG,
                                      ValueLayout.JAVA_LONG,
                                      ValueLayout.ADDRESS));
        
        // Create method handle for qsortCompare
        MethodHandle comparHandle = MethodHandles.lookup()
            .findStatic(Qsort.class,
                        "qsortCompare",
                        MethodType.methodType(int.class,
                                              MemorySegment.class,
                                              MemorySegment.class));
                                              
        // Create a Java description of a C function implemented by a Java method
        // int qsortCompare(const void *elem1, const void *elem2);
        FunctionDescriptor qsortCompareDesc = FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT),
            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT));


        // Create function pointer for qsortCompare
        // int (*compareFunc)(const void *, const void *);
        MemorySegment compareFunc = linker.upcallStub(comparHandle,
                                                      qsortCompareDesc,
                                                      Arena.ofAuto());        
        
        try (Arena arena = Arena.ofConfined()) {                    
        
            // Allocate off-heap memory and store unsortedArray in it                
            MemorySegment array = arena.allocateFrom(ValueLayout.JAVA_INT,
                                                      unsortedArray);        
                    
            // Call qsort
            // void qsort(void *base, size_t nmemb, size_t size, int (*compar)(const void *, const void *));
            qsort.invoke(array,
                        (long)unsortedArray.length,
                        ValueLayout.JAVA_INT.byteSize(),
                        compareFunc);
            
            // Access off-heap memory
            sorted = array.toArray(ValueLayout.JAVA_INT);              
        }
        return sorted;
    }        
    
    public static void main(String[] args) {
        try { 
            int[] sortedArray = InvokeQsort.qsortTest(new int[] { 0, 9, 3, 4, 6, 5, 1, 8, 2, 7 });
            for (int num : sortedArray) {
                System.out.print(num + " ");
            }
            System.out.println();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
