package com.patelheggere.tripplanner.utils;

import android.util.Log;

import com.patelheggere.tripplanner.model.EventDetailModel;

import java.util.List;
import java.util.Stack;

public class TSPNearestNeighbour
{
    private static final String TAG = "TSPNearestNeighbour";
    private int numberOfNodes;
    private Stack<Integer> stack;

    public TSPNearestNeighbour()
    {
        stack = new Stack<Integer>();
    }

    public int[] tsp(int adjacencyMatrix[][], List<EventDetailModel> filteredList)
    {
        numberOfNodes = adjacencyMatrix[1].length - 1;
        int[] visited = new int[numberOfNodes + 1];
        visited[1] = 1;
        int array[] = new int[filteredList.size()];
        stack.push(1);
        int element, dst = 0, i;
        int min = Integer.MAX_VALUE;
        boolean minFlag = false;
        Log.d(TAG, "tsp: "+filteredList.get(0).getPlaceName());
        System.out.print(1 + "\t");
        int k = 0;
        array[k] = 0;

        while (!stack.isEmpty())
        {
            element = stack.peek();
            i = 1;
            min = Integer.MAX_VALUE;
            while (i <= numberOfNodes)
            {
                if (adjacencyMatrix[element][i] > 1 && visited[i] == 0)
                {
                    if (min > adjacencyMatrix[element][i])
                    {
                        min = adjacencyMatrix[element][i];
                        dst = i;
                        minFlag = true;
                    }
                }
                i++;
            }
            if (minFlag)
            {
                visited[dst] = 1;
                stack.push(dst);
                k++;
                array[k]=dst-1;
                Log.d(TAG, "tsp: "+filteredList.get(dst-1).getPlaceName());
                System.out.print(dst + "\t");
                minFlag = false;
                continue;
            }
            stack.pop();
        }
        return array;
    }

    /*public static void main(String... arg)
    {
        int number_of_nodes;
        Scanner scanner = null;
        try
        {
            System.out.println("Enter the number of nodes in the graph");
            scanner = new Scanner(System.in);
            number_of_nodes = scanner.nextInt();
            int adjacency_matrix[][] = new int[number_of_nodes + 1][number_of_nodes + 1];
            System.out.println("Enter the adjacency matrix");
            for (int i = 1; i <= number_of_nodes; i++)
            {
                for (int j = 1; j <= number_of_nodes; j++)
                {
                    adjacency_matrix[i][j] = scanner.nextInt();
                }
            }
            for (int i = 1; i <= number_of_nodes; i++)
            {
                for (int j = 1; j <= number_of_nodes; j++)
                {
                    if (adjacency_matrix[i][j] == 1 && adjacency_matrix[j][i] == 0)
                    {
                        adjacency_matrix[j][i] = 1;
                    }
                }
            }
            System.out.println("the citys are visited as follows");
            TSPNearestNeighbour tspNearestNeighbour = new TSPNearestNeighbour();
            tspNearestNeighbour.tsp(adjacency_matrix);
        } catch (InputMismatchException inputMismatch)
        {
            System.out.println("Wrong Input format");
        }
        scanner.close();
    }*/
}
