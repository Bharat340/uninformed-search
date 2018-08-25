
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author pbharat
 */
public class search {

    public static Integer[][] nursery;
    public static Integer size;
    public static HashMap<Integer, ArrayList<Integer>> treesColumns = new HashMap<>();  // Key = column index, value = row index array
    public static HashMap<Integer, ArrayList<Integer>> treesRows = new HashMap<>(); // Key = row index, value = column index array
    public static Integer totalLizards;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String method = readInput();
        System.out.println("Method: " + method + " Size: " + size + " Lizards: " + totalLizards + " Total trees: " + treesRows.size());

        if (size < treesColumns.size()) {
            writeOutput(false);
            return;
        }
        if (size < 4 && treesColumns.isEmpty()) {
            writeOutput(false);
            return;
        }
        if ("DFS".equalsIgnoreCase(method)) {
            Stack stack = new Stack();
            stack.push(new StateNode());
            boolean status = DFS(stack);
            writeOutput(status);
        } else if ("BFS".equalsIgnoreCase(method)) {
            NodeQueue<StateNode> queue = new NodeQueue<>();
            queue.enqueue(new StateNode());
            boolean status = BFS(queue);
            writeOutput(status);
        } else if ("SA".equalsIgnoreCase(method)) {
            boolean status = SA(startTime);
            writeOutput(status);
        }
    }

    public static String readInput() {
        String path = "input.txt";
        String method = null;
        try (FileReader fileReader = new FileReader(path); BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line = null;
            if (null != (line = bufferedReader.readLine())) {
                method = line;
            }
            if (null != (line = bufferedReader.readLine())) {
                size = Integer.parseInt(line);
            }
            if (null != (line = bufferedReader.readLine())) {
                totalLizards = Integer.parseInt(line);
            }
            nursery = new Integer[size][size];
            for (Integer i = 0; i < size && null != (line = bufferedReader.readLine()); i++) {
                for (Integer j = 0; j < size; j++) {
                    nursery[i][j] = Character.getNumericValue(line.charAt(j));
                    if (2 == nursery[i][j]) {
                        insertTreePosition(i, j);
                    }
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return method;
    }

    public static void insertTreePosition(Integer row, Integer column) {
        ArrayList<Integer> rowList;
        if (treesColumns.containsKey(column)) {
            rowList = treesColumns.get(column);
            rowList.add(row);
        } else {
            rowList = new ArrayList<>();
            rowList.add(row);
            treesColumns.put(column, rowList);
        }
        ArrayList<Integer> columnList;
        if (treesRows.containsKey(row)) {
            columnList = treesRows.get(row);
            columnList.add(column);
        } else {
            columnList = new ArrayList<>();
            columnList.add(column);
            treesRows.put(row, columnList);
        }
    }

    public static void writeOutput(boolean status) {
        String path = "output.txt";
        try (FileWriter fileWriter = new FileWriter(path); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            if (!status) {
                bufferedWriter.write("FAIL");
            } else {
                bufferedWriter.write("OK");
                bufferedWriter.newLine();
                for (Integer i = 0; i < size; i++) {
                    StringBuilder row = new StringBuilder();
                    for (Integer j = 0; j < size; j++) {
                        row.append(String.valueOf(nursery[i][j]));
                    }
                    bufferedWriter.write(row.toString());
                    bufferedWriter.newLine();
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    public static boolean isGoalState(StateNode current) {
        if (null != current && totalLizards == current.getLizardsCount()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public static boolean isSafePositionToPlace(Integer row, Integer column, StateNode current) {
        if (2 == nursery[row][column]) {
            return false;
        }
        //row test
        ArrayList<Integer> columnList = current.getLizardsRows().containsKey(row) ? current.getLizardsRows().get(row) : null;
        if (null != columnList) {
            ArrayList<Integer> treeColumnList = treesRows.containsKey(row) ? treesRows.get(row) : null;
            if (null == treeColumnList) {
                return false;
            }
            boolean leftStop = false, rightStop = false;
            Integer i = column - 1;
            while (i >= 0 && !leftStop) {
                if (treeColumnList.contains(i)) {
                    leftStop = true;
                }
                if (columnList.contains(i)) {
                    return false;
                }
                i--;
            }
            i = column + 1;
            while (i < size && !rightStop) {
                if (treeColumnList.contains(i)) {
                    rightStop = true;
                }
                if (columnList.contains(i)) {
                    return false;
                }
                i++;
            }

        }
        //column test
        ArrayList<Integer> rowList = current.getLizardsColumns().containsKey(column) ? current.getLizardsColumns().get(column) : null;
        if (null != rowList) {
            ArrayList<Integer> treeRowList = treesColumns.containsKey(column) ? treesColumns.get(column) : null;
            if (null == treeRowList) {
                return false;
            }
            Integer maxRow = Collections.max(rowList);
            if (maxRow >= row) {
                return false;
            }
            Integer maxTreeRow = Collections.max(treeRowList);
            if (maxRow >= maxTreeRow || maxTreeRow >= row) {
                return false;
            }
        }
        //diagonal test
        Integer i = row - 1;
        boolean leftStop = false, rightStop = false;
        while (i >= 0) {
            Integer factor = row - i;
            if (!leftStop) {
                columnList = treesRows.containsKey(i) ? treesRows.get(i) : null;
                if (null != columnList && columnList.contains(column - factor)) {
                    leftStop = true;
                }
                columnList = current.getLizardsRows().containsKey(i) ? current.getLizardsRows().get(i) : null;
                if (null != columnList && columnList.contains(column - factor)) {
                    return false;
                }
            }
            if (!rightStop) {
                columnList = treesRows.containsKey(i) ? treesRows.get(i) : null;
                if (null != columnList && columnList.contains(column + factor)) {
                    rightStop = true;
                }
                columnList = current.getLizardsRows().containsKey(i) ? current.getLizardsRows().get(i) : null;
                if (null != columnList && columnList.contains(column + factor)) {
                    return false;
                }
            }
            i--;
        }
        return true;
    }

    public static StateNode action(Integer row, Integer column, StateNode current) {
        if (null == current || row >= size || column >= size) {
            return null;
        }
        StateNode child = new StateNode(current);
        ArrayList<Integer> columnList = child.getLizardsRows().containsKey(row) ? child.getLizardsRows().get(row) : new ArrayList<>();
        columnList.add(column);
        child.getLizardsRows().put(row, columnList);
        ArrayList<Integer> rowList = child.getLizardsColumns().containsKey(column) ? child.getLizardsColumns().get(column) : new ArrayList<>();
        rowList.add(row);
        child.getLizardsColumns().put(column, rowList);
        return child;
    }

    public static void setGoalNursery(StateNode goal) {
        //assume goal is never null
        for (Integer row : goal.getLizardsRows().keySet()) {
            ArrayList<Integer> columnList = goal.getLizardsRows().get(row);
            for (Integer column : columnList) {
                nursery[row][column] = 1;
            }
        }
    }

    public static StateNode getParentNode(StateNode current, ArrayList<String> path) {
        if (path.isEmpty()) {
            return current;
        }
        String lastElement = path.remove(path.size() - 1);
        String[] position = lastElement.split(",");
        Integer row = Integer.parseInt(position[0]);
        Integer column = Integer.parseInt(position[1]);
        StateNode parent = new StateNode(current);
        ArrayList<Integer> columnList = parent.getLizardsRows().containsKey(row) ? parent.getLizardsRows().get(row) : new ArrayList<>(); // never empty
        if (1 == columnList.size()) {
            parent.getLizardsRows().remove(row);
        } else {
            columnList.remove(column);
            parent.getLizardsRows().put(row, columnList);
        }
        ArrayList<Integer> rowList = parent.getLizardsColumns().containsKey(column) ? parent.getLizardsColumns().get(column) : new ArrayList<>(); //never empty
        if (1 == rowList.size()) {
            parent.getLizardsColumns().remove(column);
        } else {
            rowList.remove(row);
            parent.getLizardsColumns().put(column, rowList);
        }
        return parent;
    }

    public static boolean SA(long startTime) {
        HashSet<StateNode> exploredSet = new HashSet<>();
        ArrayList<String> path = new ArrayList<>();
        long spent = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
        StateNode current = new StateNode();
        StateNode bestState = current;
//        Integer count = 0;
        while (spent <= 290) {
            boolean exhausted = current.getLizardsCount() == 0;
            Integer rowIndex = current.getLizardsRows().isEmpty() ? 0 : Collections.max(current.getLizardsRows().keySet());
            boolean flag = false;
            ArrayList<HashMap<String, Object>> children = new ArrayList<>();
            if (!exploredSet.contains(current)) {
                for (Integer i = rowIndex; i < size && !flag; i++) {
                    ArrayList<Integer> columnList = current.getLizardsRows().get(i);
                    for (Integer j = 0; j < size; j++) {
                        boolean isLizardPresent = false;
                        if (null != columnList && columnList.contains(j)) {
                            isLizardPresent = true;
                        }
                        if (!isLizardPresent && isSafePositionToPlace(i, j, current)) {
                            StateNode child = action(i, j, current);
                            if (!exploredSet.contains(child)) {
                                if (isGoalState(child)) {
                                    setGoalNursery(child);
                                    //Success
                                    return true;
                                }
                                flag = true;
                                StringBuilder position = new StringBuilder();
                                position.append(i);
                                position.append(",");
                                position.append(j);
                                HashMap<String, Object> childMap = new HashMap<>();
                                childMap.put("path", position.toString());
                                childMap.put("node", child);
                                children.add(childMap);
                            }
                        }
                    }
                }
            }
            if (children.isEmpty()) {
                exploredSet.add(current);
                if (exhausted) {
                    //Solution space completely exhausted.
                    return false;
                }
                StateNode parent = getParentNode(current, path);
                Integer delta = parent.getLizardsCount() - current.getLizardsCount();
                double random = Math.random();
                long t = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
                t = t == 0 ? 1 : t;
                double probability = Math.exp(delta / t);
                if (random <= probability) {
                    if (0 == path.size()) {
                        current = new StateNode();
                    } else {
                        current = parent;
                    }
                }
            } else {
                Random random = new Random();
                Integer index = random.nextInt(children.size());
                HashMap<String, Object> stateMap = children.get(index);
                StateNode nextState = (StateNode) stateMap.get("node");
                String position = (String) stateMap.get("path");
                path.add(position);
                bestState = bestState.getLizardsCount() >= nextState.getLizardsCount() ? nextState : bestState;
                current = nextState;
            }
            spent = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
//            System.out.println("Iteration: " + count++ + " set size: " + exploredSet.size() + " path: " + path);
        }
        // Time out. So returning
        return false;
    }

    public static boolean DFS(Stack stack) {
        HashSet<StateNode> exploredSet = new HashSet<>();
        while (!stack.empty()) {
            StateNode currentState = (StateNode) stack.pop();
            if (isGoalState(currentState)) {
                setGoalNursery(currentState);
                //Success
                return true;
            }
            if (!exploredSet.contains(currentState)) {
                Integer rowIndex = currentState.getLizardsRows().isEmpty() ? 0 : Collections.max(currentState.getLizardsRows().keySet());
                boolean flag = false; //stop iterating when you could add at least one child
                for (Integer i = rowIndex; i < size && !flag; i++) {
                    ArrayList<Integer> columnList = currentState.getLizardsRows().get(i);
                    for (Integer j = 0; j < size; j++) {
                        boolean isLizardPresent = false;
                        if (null != columnList && columnList.contains(j)) {
                            isLizardPresent = true;
                        }
                        if (!isLizardPresent && isSafePositionToPlace(i, j, currentState)) {
                            StateNode child = action(i, j, currentState);

                            stack.push(child);
                            flag = true;
                        }
                    }
                }
                exploredSet.add(currentState);
            }
        }
        return false;
    }

    public static boolean BFS(NodeQueue queue) {
        HashSet<StateNode> exploredSet = new HashSet<>();
        while (0 != queue.size()) {
            StateNode currentState = (StateNode) queue.dequeue();
            if (isGoalState(currentState)) {
                setGoalNursery(currentState);
                //Success
                return true;
            }
            if (!exploredSet.contains(currentState)) {
                boolean flag = false;
                Integer rowIndex = currentState.getLizardsRows().isEmpty() ? 0 : Collections.max(currentState.getLizardsRows().keySet());
                for (Integer i = rowIndex; i < size && !flag; i++) {
                    ArrayList<Integer> columnList = currentState.getLizardsRows().get(i);
                    for (Integer j = 0; j < size; j++) {
                        boolean isLizardPresent = false;
                        if (null != columnList && columnList.contains(j)) {
                            isLizardPresent = true;
                        }
                        if (!isLizardPresent && isSafePositionToPlace(i, j, currentState)) {
                            StateNode child = action(i, j, currentState);
                            queue.enqueue(child);
                            flag = true;
                        }
                    }
                }
                exploredSet.add(currentState);
            }
        }
        return false;
    }
}

class StateNode {

    private HashMap<Integer, ArrayList<Integer>> lizardsColumns = new HashMap<>(); //key = column; list = rows
    private HashMap<Integer, ArrayList<Integer>> lizardsRows = new HashMap<>(); //key = row; list = columns

    StateNode() {
    }

    StateNode(StateNode another) {
        HashMap<Integer, ArrayList<Integer>> lc = another.getLizardsColumns();
        for (Integer key : lc.keySet()) {
            ArrayList<Integer> rowList = new ArrayList<>();
            for (Integer value : lc.get(key)) {
                rowList.add(value);
            }
            this.lizardsColumns.put(key, rowList);
        }
        HashMap<Integer, ArrayList<Integer>> lr = another.getLizardsRows();
        for (Integer key : lr.keySet()) {
            ArrayList<Integer> columnList = new ArrayList<>();
            for (Integer value : lr.get(key)) {
                columnList.add(value);
            }
            this.lizardsRows.put(key, columnList);
        }
    }

    public Integer getLizardsCount() {
        Integer count = 0;
        for (ArrayList<Integer> value : this.lizardsRows.values()) {
            count = count + value.size();
        }
        return count;
    }

    public HashMap<Integer, ArrayList<Integer>> getLizardsColumns() {
        return lizardsColumns;
    }

    public void setLizardsColumns(HashMap<Integer, ArrayList<Integer>> lizardsColumns) {
        this.lizardsColumns = lizardsColumns;
    }

    public HashMap<Integer, ArrayList<Integer>> getLizardsRows() {
        return lizardsRows;
    }

    public void setLizardsRows(HashMap<Integer, ArrayList<Integer>> lizardsRows) {
        this.lizardsRows = lizardsRows;
    }

    @Override
    public boolean equals(Object obj) {
        StateNode another = (StateNode) obj;
        if (!this.lizardsRows.keySet().equals(another.getLizardsRows().keySet())) {
            return false;
        }
        for (Integer i : this.lizardsRows.keySet()) {
            if (!isSimilarList(this.lizardsRows.get(i), another.getLizardsRows().get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.lizardsColumns);
        hash = 59 * hash + Objects.hashCode(this.lizardsRows);
        return hash;
    }

    public boolean isSimilarList(ArrayList<Integer> list1, ArrayList<Integer> list2) {
        if (null == list1 && null == list2) {
            return true;
        }
        if ((null == list1 && null != list2) || (null != list1 && null == list2)) {
            return false;
        }

        if (list1.size() != list2.size()) {
            return false;
        }
        for (Integer itemList1 : list1) {
            if (!list2.contains(itemList1)) {
                return false;
            }
        }
        return true;
    }

}

class NodeQueue<Node> {

    private LinkedList<Node> list = new LinkedList<>();

    public void enqueue(Node item) {
        list.addLast(item);
    }

    public Node dequeue() {
        return list.poll();
    }

    public boolean hasItems() {
        return !list.isEmpty();
    }

    public int size() {
        return list.size();
    }

}
