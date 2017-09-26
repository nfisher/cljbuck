package ca.junctionbox.cljbuck.build;

import java.util.Iterator;
import java.util.Stack;

public class SerialBuild implements Walken {
    private Stack<Node> buildStack;

    public SerialBuild() {
        this.buildStack = new Stack<>();
    }

    @Override
    public void step(final Node node, final int depth) {
        buildStack.push(node);
    }

    public Node pop() {
        if (buildStack.isEmpty()) {
            return null;
        }
        return buildStack.pop();
    }

    public boolean isEmpty() {
        return buildStack.isEmpty();
    }
}
