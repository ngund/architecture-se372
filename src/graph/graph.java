package graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: mmaxy
 * Date: 6/3/13
 * Time: 9:10 PM
 */
public class Graph {

    Point[] vertex;
    private Map<Integer, String> dict;

    {
        dict = new HashMap<Integer, String>();
        dict.put(1, "public");
        dict.put(2, "private");
    }

    public Graph(int size) throws IllegalArgumentException {
        if (size <= 0) throw new IllegalArgumentException("Количество вершин в графе должно быть строго больше 0");
        vertex = new Point[size];
    }

    public void setGraphSize(int newSize) throws IllegalArgumentException {
        if (newSize <= 0) throw new IllegalArgumentException("Количество вершин в графе должно быть строго больше 0");
        vertex = new Point[newSize];
    }

    public void setGraphFromMatrix(int[][] adjacencyMatrix) throws IllegalArgumentException {
        if (adjacencyMatrix == null) throw new IllegalArgumentException();
        if (adjacencyMatrix.length != adjacencyMatrix[0].length)
            throw new IllegalArgumentException("Матрица должна быть квадратная");
        if (adjacencyMatrix.length != vertex.length)
            throw new IllegalArgumentException("Матрица должна соответствовать количеству вершин");

        for (int i = 0; i < adjacencyMatrix.length; i++) {
            for (int j = 0; j < adjacencyMatrix[i].length; j++) {
                if (adjacencyMatrix[i][j] == 0) continue;
                if (adjacencyMatrix[i][j] > 2)
                    throw new IllegalArgumentException("В матрице можно использовать только 0 1 и 2");
                vertex[i].addUsing(j);
                vertex[j].addUsedBy(i);
                vertex[i].setAccessModifier(adjacencyMatrix[i][j]);
            }
        }
    }

    public void setGraphFromList(int[][] list) throws IllegalArgumentException {
        if (list == null) throw new IllegalArgumentException();
        if (list[0].length != 3)
            throw new IllegalArgumentException("список должен состоять из трех элементов. Neither more, nor less");
        for (int i = 0; i < list.length; i++) {
            if (list[i][2] == 0) continue;
            vertex[list[i][0]].addUsing(list[i][1]);
            vertex[list[i][1]].addUsedBy(list[i][0]);
            vertex[i].setAccessModifier(list[i][2]);
        }
    }

    public int[][] readFromFile(FileReader fr) throws Exception {
        List<int[]> res = new ArrayList<int[]>();
        BufferedReader br = new BufferedReader(fr);
        String[] splited;
        try {
            String line = br.readLine();
            if (line.equals("List")) {
                line = br.readLine();
                while (line != null) {
                    splited = line.split("\\s+");
                    if (splited.length < 3) throw new Exception("Неправильное форматирование файла");
                    res.add(new int[]{Integer.parseInt(splited[0]), Integer.parseInt(splited[1]), Integer.parseInt(splited[2])});
                    line = br.readLine();
                }
            } else if (line.equals("Matrix")) {
                //Строка - i(откуда)
                //Столбец - j(куда)
                line = br.readLine();
                splited = line.split("\\s+");
                int numberOfLines = 1;
                int[] tmp = new int[splited.length];
                for (int i = 0; i < splited.length; i++) {
                    tmp[i] = Integer.parseInt(splited[i]);
                }
                res.add(tmp);
                line = br.readLine();
                while (line != null) {
                    splited = line.split("\\s+");
                    if (splited.length != tmp.length || numberOfLines > tmp.length)
                        throw new Exception("Неправильное форматирование файла, матрица не квадратная");
                    tmp = new int[splited.length];
                    for (int i = 0; i < splited.length; i++) {
                        tmp[i] = Integer.parseInt(splited[i]);
                    }
                    res.add(tmp);
                    numberOfLines++;
                    line = br.readLine();
                }
            } else {
                throw new Exception("Неправильное форматирование файла");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
        }
        return (int[][]) res.toArray();
    }

    public void findLabels() {
        List<Point> vert = new ArrayList<Point>();

        Collections.addAll(vert, vertex);

        findL(vert);
        int i = 1;
        while (searchNextLabel(i)) i++;
    }

    private boolean searchNextLabel(int currentLabel) {
        List<Point> graph = new ArrayList<Point>();
        Map<Integer, Integer> mapGraphToVer = new HashMap<Integer, Integer>();
        Map<Integer, Integer> mapVerToGraph = new HashMap<Integer, Integer>();

        for (int i = 0; i < vertex.length; i++) {
            if (vertex[i].getLabel() == 0) {
                graph.add(new Point());
                mapGraphToVer.put(graph.size(), i);
                mapVerToGraph.put(i, graph.size());
            }
        }
        if (graph.size() == 0) return false;
        for (int i = 0; i < graph.size(); i ++) {
            graph.get(i).setAccessModifier(vertex[mapGraphToVer.get(i)].getAccessModifier());
            List<Integer> usedBy = vertex[mapGraphToVer.get(i)].getUsedBy();
            for (Integer u : usedBy) {
                if (mapVerToGraph.containsKey(u)) {
                    graph.get(i).addUsedBy(mapVerToGraph.get(u));
                }
            }
            List<Integer> using = vertex[mapGraphToVer.get(i)].getUsing();
            for (Integer u : using) {
                if (mapVerToGraph.containsKey(u)) {
                    graph.get(i).addUsing(mapVerToGraph.get(u));
                }
            }
        }
        findL(graph);

        for (int i = 0; i < graph.size(); i++) {
            if (graph.get(i).getLabel() != 0) {
                vertex[mapGraphToVer.get(i)].setLabel(i+1);
            }
        }
        return true;
    }

    private void findL(List<Point> graph) {
        for (Point aVertex : graph) {
            if (aVertex.getSizeOfUsing() == 0 && aVertex.getAccessModifier() != 2) {
                aVertex.setLabel(1);
            }
        }
        int[] crossHandled = findCrossHandledModules(graph);
        for (int aCrossHandled : crossHandled) {
            graph.get(aCrossHandled).setLabel(1);
        }
        List<Integer> loop = findFreeLoop(graph);
        for (int aLoop : loop) {
            graph.get(aLoop).setLabel(1);
        }
    }

    private int[] findCrossHandledModules(List<Point> graph) {
        List<Integer> res = new ArrayList<Integer>();
        List<Integer> alreadyChecked = new ArrayList<Integer>();
        for (int i = 0; i < graph.size(); i++) {
            if (!alreadyChecked.contains(i)) {
                List<Integer> tmp = new ArrayList<Integer>();
                if (check(graph, tmp, i)) {
                    for (Integer aTmp : tmp)
                    if (!res.contains(aTmp))
                        res.add(aTmp);
                }
                alreadyChecked.add(i);
                for (Integer aTmp : tmp)
                    if (!alreadyChecked.contains(aTmp))
                        alreadyChecked.add(aTmp);
            }
        }
        int[] resArray = new int[res.size()];
        for (int i = 0; i < res.size(); i++) {
            resArray[i] = res.get(i);
        }
        return resArray;
    }

    private boolean check(List<Point> graph, List<Integer> alreadyChecked, int v) {
        boolean res = true;
        List<Integer> using = graph.get(v).getUsing();
        if (alreadyChecked.contains(v)) {
            return true;
        }
        alreadyChecked.add(v);
        for (int j = 0; j < using.size(); j++) {
            if (graph.get(using.get(j)).getUsing().contains(v)) {
                res = res && check(graph, alreadyChecked, j);
            } else {
                res = false;
            }
        }
        return res;
    }

    /***
     * Найди свободный цикл, как в примере CDE, то есть множество вершин, которые используют друг друга и
     * модули, не имеющие зависимостей никаких, кроме базового слоя.
     * @param graph граф, в котором надо искать
     * @return массив индексов таких точек
     */
    private List<Integer> findFreeLoop(List<Point> graph) {
        List<Integer> res = new ArrayList<Integer>();
        //TODO здесь нужно искать свободный цикл, то есть такой, который использует либо себя, либо еще модули с нижнего уровня
        return res;
    }

    private class Point {

        private int accessModifier;
        private int label;
        private List<Integer> using = new ArrayList<Integer>();
        private List<Integer> usedBy = new ArrayList<Integer>();

        int getAccessModifier() {
            return accessModifier;
        }

        void setAccessModifier(int accessModifier) {
            this.accessModifier = accessModifier;
        }

        String getStringAccessModifier() {
            return dict.get(this.accessModifier);
        }

        int getLabel() {
            return label;
        }

        void setLabel(int label) {
            this.label = label;
        }

        List<Integer> getUsing() {
            return using;
        }

        void setUsing(List<Integer> using) {
            this.using = using;
        }

        void addUsing(int newPoint) {
            this.using.add(newPoint);
        }

        int getSizeOfUsing() {
            return this.using.size();
        }

        List<Integer> getUsedBy() {
            return usedBy;
        }

        void setUsedBy(List<Integer> usedBy) {
            this.usedBy = usedBy;
        }

        void addUsedBy(int newPoint) {
            this.usedBy.add(newPoint);
        }

        int getSizeOfUsedBy() {
            return this.usedBy.size();
        }
    }

}