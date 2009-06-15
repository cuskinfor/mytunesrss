package de.codewave.mytunesrss;

/**
 * de.codewave.mytunesrss.WeightedString
 */
public class WeightedTrackId implements Comparable {
    private String myId;
    private float myWeight;

    public WeightedTrackId(String id, float weight) {
        myId = id;
        myWeight = weight;
    }

    public String getId() {
        return myId;
    }

    public float getWeight() {
        return myWeight;
    }

    public int compareTo(Object o) {
        if (!(o instanceof WeightedTrackId)) {
            throw new IllegalArgumentException("Cannot compare " + getClass() + " to " + o.getClass());
        }
        return (int) Math.signum(myWeight - ((WeightedTrackId) o).myWeight);
    }

    @Override
    public int hashCode() {
        return myId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof WeightedTrackId)) {
            return false;
        }
        return myId == ((WeightedTrackId) obj).myId;
    }
}