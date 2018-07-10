package com.kayhut.fuse.executor.cursor.discrete;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.model.transport.cursor.CreateHierarchyFlattenCursorRequest;
import com.opencsv.CSVWriter;
import javaslang.collection.Stream;
import javaslang.control.Option;

import java.io.StringWriter;
import java.util.*;

public class HierarchyFlattenCursor implements Cursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new HierarchyFlattenCursor(
                    new PathsTraversalCursor((TraversalCursorContext)context),
                    (CreateHierarchyFlattenCursorRequest)context.getCursorRequest());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public HierarchyFlattenCursor(PathsTraversalCursor innerCursor, CreateHierarchyFlattenCursorRequest cursorRequest) {
        this.innerCursor = innerCursor;
    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResultBase getNextResults(int numResults) {
        Map<String, Set<String>> childMap = new HashMap<>();
        List<String> roots = new ArrayList<>();
        Set<String> allVertices = new HashSet<>();

        AssignmentsQueryResult nextResults;
        do{
            nextResults = this.innerCursor.getNextResults(numResults);
            for (Assignment assignment : nextResults.getAssignments()) {
                Entity child = Stream.ofAll(assignment.getEntities()).find(e -> e.geteTag().contains("Child")).get();
                Option<Entity> parent = Stream.ofAll(assignment.getEntities()).find(e -> e.geteTag().contains("Parent"));

                allVertices.add(child.geteID());
                if(!parent.isEmpty()){
                    Set<String> children = childMap.computeIfAbsent(parent.get().geteID(), p -> new HashSet<>());
                    children.add(child.geteID());
                    allVertices.add(parent.get().geteID());
                }else{
                    roots.add(child.geteID());
                }
            }
        }
        while(nextResults.getSize() > 0);

        Set<String> handledVertices = new HashSet<>();

        List<HierarchyPath> paths = new ArrayList<>();
        Stack<String> nodeStack = new Stack<>();
        for (String root : roots) {
            paths.addAll(visitNode(root, nodeStack, handledVertices, childMap));
        }

        if(handledVertices.size() != allVertices.size()){
            throw new IllegalArgumentException("Hierarchy contains cycle, cannot flatten");
        }

        CsvQueryResult.Builder builder = CsvQueryResult.Builder.instance();

        for (HierarchyPath path : paths) {
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer, separator, quotechar,"");
            csvWriter.writeNext(new String[] {path.getParent(), path.getChild(), Integer.toString(path.getDist())});
            builder.withLine(writer.getBuffer().toString());
        }
        return builder.build();
    }
    //endregion

    //region Private Methods
    private Collection<? extends HierarchyPath> visitNode(String node, Stack<String> nodeStack, Set<String> handledVertices,  Map<String, Set<String>> childMap) {
        List<HierarchyPath> paths = new ArrayList<>();

        if(!handledVertices.contains(node)){
            paths.add(new HierarchyPath(node, node, 0));
        }

        handledVertices.add(node);

        for(int i = 0; i < nodeStack.size(); i++){
            String parent = nodeStack.get(i);
            int dist = nodeStack.size() - i;
            paths.add(new HierarchyPath(parent, node, dist));
            paths.add(new HierarchyPath(node, parent, -1 * dist));
        }

        nodeStack.push(node);

        Set<String> stackSet = new HashSet<>(nodeStack);
        if(stackSet.size() < nodeStack.size()){
            throw new IllegalArgumentException("Hierarchy contains cycle, cannot flatten");
        }

        for (String child : childMap.getOrDefault(node, new HashSet<>())){
            paths.addAll(visitNode(child, nodeStack, handledVertices, childMap));

        };

        nodeStack.pop();


        return paths;
    }
    //endregion

    //region Fields
    private PathsTraversalCursor innerCursor;
    private char separator = ',';
    private char quotechar = '"';
    //endregion

    //region HierarchyPath
    private class HierarchyPath{
        private String parent;
        private String child;
        private int dist;

        public HierarchyPath(String parent, String child, int dist) {
            this.parent = parent;
            this.child = child;
            this.dist = dist;
        }

        public String getParent() {
            return parent;
        }

        public void setParent(String parent) {
            this.parent = parent;
        }

        public String getChild() {
            return child;
        }

        public void setChild(String child) {
            this.child = child;
        }

        public int getDist() {
            return dist;
        }

        public void setDist(int dist) {
            this.dist = dist;
        }
    }
    //endregion
}