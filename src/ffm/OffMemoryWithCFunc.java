package ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

public class OffMemoryWithCFunc {

    public static void main(String[] args) throws Throwable {
        String pattern = "testing";
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment nativeString = arena.allocateFrom(pattern);

            Linker linker = Linker.nativeLinker();
            SymbolLookup stdLib = linker.defaultLookup();
            MemorySegment strdup_addr = stdLib.find("strdup")
                    .orElseThrow(() -> new RuntimeException("strdup not found"));


            var layout = MemoryLayout.sequenceLayout(Long.MAX_VALUE, JAVA_BYTE);
            FunctionDescriptor strdup_sig = FunctionDescriptor.of(
                ValueLayout.ADDRESS.withTargetLayout(layout),
                ValueLayout.ADDRESS.withTargetLayout(layout)
                                                                 );

            MethodHandle strdup_handle = linker.downcallHandle(strdup_addr, strdup_sig);


            MemorySegment duplicatedAddress = (MemorySegment) strdup_handle.invokeExact(nativeString);


            // getString(0) return all the string coz it reads until it finds a null character \0
            System.out.println("Duplicated address: " + duplicatedAddress.getString(0));

            for (int i = pattern.length() - 1; i >= 0; i--) {
                System.out.println(duplicatedAddress.getString(i));
            }
        }

    }
}
