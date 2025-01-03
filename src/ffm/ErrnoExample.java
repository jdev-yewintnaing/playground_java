package ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

public class ErrnoExample {

    static void invokeFopen(String path, String mode) throws Throwable {

        // Setup handles
        Linker.Option ccs = Linker.Option.captureCallState("errno");
        StructLayout capturedStateLayout = Linker.Option.captureStateLayout();
        VarHandle errnoHandle = capturedStateLayout.varHandle(MemoryLayout.PathElement.groupElement("errno"));

        // log C Standard Library function
        Linker linker = Linker.nativeLinker();
        SymbolLookup stdLib = linker.defaultLookup();
        MethodHandle fopen =linker.downcallHandle(stdLib.find("fopen").orElseThrow(),
                                                  FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS),
                                                  ccs);

        // strerror C Standard Library function
        MethodHandle strerror = linker.downcallHandle(
            stdLib.find("strerror").orElseThrow(),
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

        // Actual invocation
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment capturedState = arena.allocate(capturedStateLayout);
            MemorySegment location = arena.allocateFrom(path);
            MemorySegment openMode =  arena.allocateFrom(mode);

            var result = (MemorySegment) fopen.invokeExact(capturedState, location, openMode);

            if (result.address() == 0) {
                // Get more information by consulting the value of errno:
                int errno = (int) errnoHandle.get(capturedState, 0);
                System.out.println("errno: " + errno); // 2

                // Convert errno code to a string message:
                String errrorString = ((MemorySegment) strerror.invokeExact(errno))
                    .reinterpret(Long.MAX_VALUE).getString(0);
                System.out.println("errno string: " + errrorString);
            }
        }
    }

    public static void main(String[] args) {

        var path = args.length > 0 ? args[0] : "non-existing-file.txt";
        var mode = args.length > 1 ? args[1] : "r";

        try {
            invokeFopen(path, mode);
        } catch (Throwable t) {
            t.printStackTrace();
        }


    }
}
