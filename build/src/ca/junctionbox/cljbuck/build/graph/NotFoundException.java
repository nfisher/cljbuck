package ca.junctionbox.cljbuck.build.graph;

public class NotFoundException extends Exception {
    public NotFoundException(String name) {
        super(name + " not found in graph.");
    }
}
