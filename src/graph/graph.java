package graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: mmaxy
 * Date: 6/3/13
 * Time: 9:10 PM
 */
public class graph {

    point[] vertex;

    private Map<Integer, String> dict;

    {
        dict = new HashMap<Integer, String>();
        dict.put(1, "public");
        dict.put(2, "private");
    }

    private class point {

        private int accessModifier;
        private int label;
        private List<Integer> using = new ArrayList<Integer>();
        private List<Integer> usedBy = new ArrayList<Integer>();

        int getAccessModifier() {
            return accessModifier;
        }

        String getStringAccessModifier() {
            return dict.get(this.accessModifier);
        }

        void setAccessModifier(int accessModifier) {
            this.accessModifier = accessModifier;
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

    public graph(int size) throws IllegalArgumentException {
        if (size <= 0) throw new IllegalArgumentException("Количество вершин в графе должно быть строго больше 0");
        vertex = new point[size];
    }

    public void setGraphSize(int newSize) throws IllegalArgumentException {
        if (newSize <= 0) throw new IllegalArgumentException("Количество вершин в графе должно быть строго больше 0");
        vertex = new point[newSize];
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            br.close();
        }
        return (int[][]) res.toArray();
    }

    public void findLabels() {
        //TODO implement this method
    }

}
