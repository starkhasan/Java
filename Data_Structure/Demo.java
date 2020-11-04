import java.io.*;
import java.util.*;

class Graph{
    private int V;
    private ArrayList<ArrayList<Integer>> adj;
    void graphInit(int vertices){
        V = vertices;
        adj = new ArrayList<ArrayList<Integer>>();
        for(int i=0;i<vertices;i++){
            adj.add(new ArrayList<Integer>());
        }
    }

    void addEdge(int u,int v){
        adj.get(u).add(v);
    }

    void printGraph(){
        for(int i=0;i<V;i++){
            System.out.print(i+" -> ");
            for(int j=0;j<adj.get(i).size();j++){
                System.out.print(adj.get(i).get(j)+" -> ");
            }
            System.out.print(" NULL");
            System.out.println();
        }
    }

    void DFSUntil(int start,boolean[] visited){
        visited[start] = true;
        System.out.print(start+" -> ");
        Iterator iterator = adj.get(start).iterator();
        while(iterator.hasNext()){
            int temp = (int)iterator.next();
            if(!visited[temp])
                DFSUntil(temp, visited);
        }
    }

    void DFS(int start){
        boolean[] visited = new boolean[V];
        DFSUntil(start,visited);
    }

    void BFS(int start){
        boolean[] visited = new boolean[V];
        visited[start] = true;
        LinkedList<Integer> queue = new LinkedList<>();
        queue.add(start);
        while(queue.size()!=0){
            int temp = queue.poll();
            System.out.print(temp+" -> ");
            Iterator iterator = adj.get(temp).iterator();
            while(iterator.hasNext()){
                int temp1 = (int)iterator.next();
                if(!visited[temp1]){
                    visited[temp1] = true;
                    queue.add(temp1);
                }
            }
        }
    }

    void topologicalSortUtil(int v, boolean visited[], Stack<Integer> stack) { 
        visited[v] = true; 
        Integer i; 
        Iterator<Integer> it = adj.get(v).iterator(); 
        while (it.hasNext()) { 
            i = it.next(); 
            if (!visited[i]) 
                topologicalSortUtil(i, visited, stack); 
        } 
        stack.push(v); 
    }

    void topoligical(int start){
        boolean[] visited = new boolean[V];
        Arrays.fill(visited,false);
        Stack<Integer> stack = new Stack<>();
        for(int i=0;i<V;i++){
            if(!visited[i])
                topologicalSortUtil(i,visited,stack);
        }
        while(stack.size()>0)
            System.out.print(stack.pop()+" ");
    }

    boolean isCyclicUntil(int i,boolean[] visited,boolean[] recStack){
        visited[i] = true; 
        recStack[i] = true; 
        List<Integer> children = adj.get(i);  
        for (Integer c: children){
            if (recStack[c]) 
                return true;
            else if(visited[i])
                isCyclicUntil(c, visited, recStack);
        }
        recStack[i] = false; 
        return false; 
 
    }
    boolean isCycle(){
        boolean cyclePresent = false;
        boolean[] visited = new boolean[V];
        boolean[] untrack = new boolean[V];
        Arrays.fill(visited,false);
        Arrays.fill(untrack,false);
        for(int i=0;i<V;i++){
            if(isCyclicUntil(i, visited, untrack)){
                cyclePresent = true;
                break;
            }
        }
        return cyclePresent;
    }
}
class Demo{
    public static void main(String[] args) throws IOException{
        BufferedReader buffer = new BufferedReader(new FileReader("InputDemo.txt"));
        boolean isFirst = true;
        String input = "";
        Graph graph = new Graph();
        while((buffer.readLine())!=null){
            if(isFirst){
                isFirst = false;
                graph.graphInit(Integer.parseInt(buffer.readLine()));
            }else{
                input = buffer.readLine();
                String[] strAr = input.split(" ");
                graph.addEdge(Integer.parseInt(strAr[0]), Integer.parseInt(strAr[1]));
            }
        }
        graph.printGraph();
        System.out.println("\nDepth First Search : ");
        graph.DFS(2);
        System.out.println("\nBreadth First Search : ");
        graph.BFS(2);
        System.out.println();
        if(graph.isCycle())
            System.out.println("Cycle Present");
        else{
            System.out.println("\nTopological Sort : ");
            graph.topoligical(5);
        }
        buffer.close();
    }
}

/**
 * input one
 
 6

5 2

5 0

4 0

4 1

2 3

3 1


*input second
4

0 1

0 2

1 2

2 0

2 3

3 3
 */
 
