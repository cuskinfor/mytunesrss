package de.codewave.mytunesrss.rest.representation;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Representation of a partial list. The representation contains the partial list (depending on the request this
 * might as well be the complete list) and the total number of items in the complete list.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PartialListRepresentation<T> implements RestRepresentation {
    private List<T> myItems;
    private Integer myTotalCount;

    public PartialListRepresentation() {
    }

    public PartialListRepresentation(List<T> items, int totalCount) {
        myItems = items;
        myTotalCount = totalCount;
    }

    /**
     * List of items. This might be a partial list depending on the request.
     */
    public List<T> getItems() {
        return myItems;
    }

    public void setItems(List<T> items) {
        myItems = items;
    }

    /**
     * Total number of items.
     */
    public Integer getTotalCount() {
        return myTotalCount;
    }

    public void setTotalCount(Integer totalCount) {
        myTotalCount = totalCount;
    }
}
