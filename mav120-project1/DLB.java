import java.util.ArrayList;

public class DLB {
    public int numOfSuggestions = 5;

    private final char TERMINATOR = '$';

    private Node root = null;
    private Node cachedNodeParent = null;
    private Node cachedNode = null;
    private StringBuilder prefix;


    // SEARCH METHOD
    public void startNewSearch() {
        prefix = new StringBuilder();
        cachedNode = root;
    }

    public ArrayList<String> search(char c, int numOfSuggestions) {
        this.numOfSuggestions = numOfSuggestions;
        return search(c);
    }

    public ArrayList<String> search(char c) {
        prefix.append(c);
        Node current = cachedNode;
        while(current != null) {
            if(current.val == c) break;
            current = current.next;
        }

        if(current == null) {
            // Could not find the character in this sub list
            // Now set cachedNode to null to stop searching this tree on new characters
            cachedNode = null;
            return null;
        }

        cachedNodeParent = current;
        cachedNode = current.child;

        if(numOfSuggestions <= 0) return null;
        StringBuilder str = new StringBuilder(prefix);
        ArrayList<String> predictions = new ArrayList<>(numOfSuggestions);
        getWords(cachedNode, predictions, str);
        return predictions;

    }
    private void getWords(Node current, ArrayList<String> predictions, StringBuilder str) {
        if(current == null) return;
        if(predictions.size() >= numOfSuggestions) return;
        if(current.val == TERMINATOR) {
            predictions.add(str.toString());
        }

        while(current != null) {
            str.append(current.val);
            // Add all words underneath as predictions:
            getWords(current.child, predictions, str);
            str.deleteCharAt(str.length()-1);
            current = current.next;
        }
    }

    // DELETE

    public void deleteCurrentWordWithSuffix(String suffix) {
        if(cachedNode == null) throw new UnsupportedOperationException("Search not started. Cannot delete");
        delete(suffix, cachedNodeParent);
    }

    private void delete(String word, Node current) {
        Node parent = current;
        Node child = parent.child;
        for(char c : word.toCharArray()) {
            while(child != null) {
                if(child.val == c) break;
                child = child.next;
            }
            parent = child;
            if(parent == null) throw new UnsupportedOperationException("Word not in DLB");
            child = child.child;
        }

        if(child.val == TERMINATOR) {
            parent.child = child.next;
        } else {
            // In the case the first child of the word isn't the TERMINATOR
            // Shouldn't happen, but in case it does, delete the terminator from the list
            while(child.next != null) {
                if(child.next.val == TERMINATOR) {
                    child.next = child.next.next;
                    break;
                }
                child.next = child.next.next;
            }
        }
    }

    // ADD METHOD

    public void add(String word) {
        add(word+TERMINATOR, 0, root);
    }

    private Node addCharacterToList(char currentChar, Node prev) {
        Node current = new Node(currentChar);
        if(prev == null) {
            root = current;
        } else {
            prev.next = current;
        }
        return current;
    }


    private void addRemainingCharacters(int currentIndex, String word, Node currentNode) {
        int i = currentIndex+1;
        while(i < word.length()) {
            currentNode.child = new Node(word.charAt(i));
            currentNode = currentNode.child;
            i++;
        }
    }

    private void add(String word, int currentIndex, Node currentNode) {
        if(currentIndex >= word.length()) return;

        char currentChar = word.charAt(currentIndex);

        Node prev = currentNode;
        while(currentNode != null) {
            if(currentNode.val == currentChar) break;
            prev = currentNode;
            currentNode = currentNode.next;
        }

        if(currentNode == null) {
            currentNode = addCharacterToList(currentChar, prev);
        }

        if(currentNode.child == null) {
            // The character has no child list, iteratively add the rest of the word
            addRemainingCharacters(currentIndex, word, currentNode);
            return;
        }

        // Recursively add next character
        add(word, currentIndex+1, currentNode.child);
    }

    ////// NODE CLASS

    private class Node {
        public char val;
        public Node child = null;
        public Node next = null;

        public Node(char val) {
            this.val = val;
        }

        public String toString() {
            return ""+val;
        }
    }

    private void printHelper(StringBuilder str, Node current) {
        str.append('[');
        Node c = current;
        while(c != null) {
            str.append(c.val + ",");
            c = c.next;
        }
        str.append("]\n");
        c = current;
        while(c != null) {
            str.append(c.val +":");
            printHelper(str, c.child);
            c = c.next;
        }
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        printHelper(str, root);
        return str.toString();
    }
}