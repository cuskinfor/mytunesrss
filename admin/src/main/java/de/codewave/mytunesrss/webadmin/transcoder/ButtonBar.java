package de.codewave.mytunesrss.webadmin.transcoder;

import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import de.codewave.vaadin.ComponentFactory;

public class ButtonBar extends Panel {

    public ButtonBar(ComponentFactory componentFactory, Button... buttons) {
        addStyleName("light");
        setContent(componentFactory.createHorizontalLayout(false, true));
        for (Button button : buttons) {
            addComponent(button);
        }
    }
}
