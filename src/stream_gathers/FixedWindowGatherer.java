package stream_gathers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

class FixedWindowGatherer implements Gatherer<Integer, List<Integer>, List<Integer>> {
    private final int windowSize;

    public FixedWindowGatherer(int windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public Supplier<List<Integer>> initializer() {
        // Initialize an empty list to collect elements for the current window
        return ArrayList::new;
    }

    @Override
    public Integrator<List<Integer>, Integer, List<Integer>> integrator() {
        // Integrate each element into the current window
        return (state, element, downstream) -> {
            state.add(element);
            if (state.size() == windowSize) {
                // Push the full window downstream and reset state
                downstream.push(new ArrayList<>(state));
                state.clear();
            }
            return true; // Continue processing
        };
    }



    @Override
    public BiConsumer<List<Integer>, Downstream<? super List<Integer>>> finisher() {
        // Handle any remaining elements at the end of the stream
        return (state, downstream) -> {
            if (!state.isEmpty()) {
                downstream.push(new ArrayList<>(state));
            }
        };
    }
}