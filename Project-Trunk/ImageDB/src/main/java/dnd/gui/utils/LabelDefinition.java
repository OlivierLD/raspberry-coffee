package dnd.gui.utils;

import java.io.Serializable;

public class LabelDefinition implements Serializable {
    private String label;
    private int occurences;

    public LabelDefinition(String label, int occurences) {
        this.label = label;
        this.occurences = occurences;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getOccurences() {
        return occurences;
    }

    public void setOccurences(int occurences) {
        this.occurences = occurences;
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", this.label, this.occurences);
    }
}
