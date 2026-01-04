package view;

import javafx.scene.layout.StackPane;
import java.util.HashMap;
import java.util.Map;

public class CardPane extends StackPane {
    private final Map<String, javafx.scene.Node> cards = new HashMap<>();

    // Mirip contentPanel.add(panel, "nama")
    public void addCard(String name, javafx.scene.Node node) {
        cards.put(name, node);
        getChildren().add(node);
        node.setVisible(false);
    }

    // Mirip cardLayout.show(contentPanel, "nama")
    public void showCard(String name) {
        getChildren().forEach(n -> n.setVisible(false));
        javafx.scene.Node node = cards.get(name);
        if (node != null) {
            node.setVisible(true);
            node.toFront();
        }
    }
}