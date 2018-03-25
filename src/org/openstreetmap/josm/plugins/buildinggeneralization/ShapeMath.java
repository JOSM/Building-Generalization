// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.buildinggeneralization;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.Logging;

public class ShapeMath {
    static Way containingWay;

    public static WaySegment getClosestSegment(Way building, WaySegment roadSegment){
        double maxDistance = Double.MAX_VALUE;
        WaySegment closestSegment = null;
    
        for(int i=0;i<building.getNodesCount()-1;i++){

            WaySegment currentSegment = new WaySegment(building, i);
            EastNorth currentSegmentCentroid = getCentroid(currentSegment);
            Point p = new Point(currentSegmentCentroid.getX(), currentSegmentCentroid.getY());
            Point x1 = new Point(roadSegment.getFirstNode().getEastNorth().getX(), roadSegment.getFirstNode().getEastNorth().getY());
            Point x2 = new Point(roadSegment.getSecondNode().getEastNorth().getX(), roadSegment.getSecondNode().getEastNorth().getY());
            double distance = Point.distance(x1,x2,p);
            
            if(distance < maxDistance){
                maxDistance = distance;
                closestSegment = new WaySegment(building,i);
            }
        }
        return closestSegment;
    }
    
    public static String formatSegment(WaySegment segment){
        String r = "\n Segment \n firstNode: x=";
        
        r += segment.getFirstNode().getCoor().getX() + ", y=";
        r += segment.getFirstNode().getCoor().getY();
        r += "\n secondNode: x=";
        r += segment.getSecondNode().getCoor().getX() + ", y=";
        r += segment.getSecondNode().getCoor().getY();
        return r;
    }
    
    public static void rotate(Node node, double angle, EastNorth center) {
        node.setEastNorth(getRotation(node.getEastNorth(), angle, center));
    }

    public static void rotate(Way way, double angle, EastNorth center) {
        Set<Node> nodesSet = new HashSet<>();
        List<Node> wayNodes = way.getNodes();
        for (Node node : wayNodes) {
            nodesSet.add(node);
        }
        EastNorth allNodesCenter = ShapeMath.getCentroid(nodesSet);
        Iterator<Node> it = nodesSet.iterator();
        while(it.hasNext()){
            rotate(it.next(), angle, allNodesCenter);
        }
        MainApplication.getLayerManager().getEditLayer().invalidate();
    }

    public static EastNorth getRotation(EastNorth originalPoint, double angle, EastNorth center) {
        double x = Math.cos(angle) * (originalPoint.getX() - center.getX())
                 - Math.sin(angle) * (originalPoint.getY() - center.getY())
                 + center.getX();
        double y = Math.sin(angle) * (originalPoint.getX() - center.getX())
                 + Math.cos(angle) * (originalPoint.getY() - center.getY())
                 + center.getY();
        return new EastNorth(x, y);
    }

    public static EastNorth getCentroid(Way way) {
        double x = 0, y = 0;
        List<Node> wayNodes = way.getNodes();

        if (way.isClosed()) {
            for (int i = 0; i < wayNodes.size() - 1; i++) {
                x += wayNodes.get(i).getEastNorth().getX();
                y += wayNodes.get(i).getEastNorth().getY();
            }
            x = x / (way.getNodesCount() - 1);
            y = y / (way.getNodesCount() - 1);
            return new EastNorth(x, y);
        }

        for (int i = 0; i < wayNodes.size(); i++) {
            x += wayNodes.get(i).getEastNorth().getX();
            y += wayNodes.get(i).getEastNorth().getY();
        }
        x = x / way.getNodesCount();
        y = y / way.getNodesCount();

        return new EastNorth(x, y);
    }

    public static EastNorth getCentroid(WaySegment segment){
        double x = 0, y = 0;
        
        x = x + segment.getFirstNode().getEastNorth().getX() + segment.getSecondNode().getEastNorth().getX();
        y = y + segment.getFirstNode().getEastNorth().getY() + segment.getSecondNode().getEastNorth().getY();
        
        x = x / 2.0;
        y = y / 2.0;
        
        return new EastNorth(x,y);
    }

    public static EastNorth getCentroid(List<Way> wayList) {
        double x = 0, y = 0;
        for (int i = 0; i < wayList.size(); i++) {
            EastNorth currentCenter = getCentroid(wayList.get(i));
            x += currentCenter.getX();
            y += currentCenter.getY();
        }
        x = x / wayList.size();
        y = y / wayList.size();
        return new EastNorth(x, y);
    }

    public static EastNorth getCentroid(Set<Node> nodes) {
        Iterator<Node> i = nodes.iterator();
        double x = 0, y = 0;
        while (i.hasNext()) {
            Node currentNode = i.next();
            x += currentNode.getEastNorth().getX();
            y += currentNode.getEastNorth().getY();
        }
        x = x / nodes.size();
        y = y / nodes.size();
        return new EastNorth(x, y);
    }

    public static void doRotate(Collection<Way> ways, Collection<Node> nodes, double angle) {
        avoidDuplicateNodesRotation(ways, nodes, angle);
    }

    public static void avoidDuplicateNodesRotation(Collection<Way> ways, Collection<Node> nodes, double angle) {
        Logging.info("doRotate() called: rotating shapes by: " + angle);
        Set<Node> nodesSet = new HashSet<>();
        for (Way way : ways) {
            List<Node> wayNodes = way.getNodes();
            for (Node node : wayNodes) {
                nodesSet.add(node);
            }
        }
        for (Node node : nodes) {
            nodesSet.add(node);
        }
        EastNorth allNodesCenter = ShapeMath.getCentroid(nodesSet);
        Iterator<Node> i = nodesSet.iterator();
        while (i.hasNext()) {
            rotate(i.next(), angle, allNodesCenter);
        }
        MainApplication.getLayerManager().getEditLayer().invalidate();
    }

    public static void align(Way firstWay, Way secondWay) {
        double x1 = firstWay.getNode(0).getEastNorth().getX();
        double x2 = firstWay.getNode(1).getEastNorth().getX();
        double x3 = secondWay.getNode(0).getEastNorth().getX();
        double x4 = secondWay.getNode(1).getEastNorth().getX();
        double y1 = firstWay.getNode(0).getEastNorth().getY();
        double y2 = firstWay.getNode(1).getEastNorth().getY();
        double y3 = secondWay.getNode(0).getEastNorth().getY();
        double y4 = secondWay.getNode(1).getEastNorth().getY();
        double requiredAngle = Math.atan2(y2 - y1, x2 - x1)
                             - Math.atan2(y4 - y3, x4 - x3);
        Logging.info("Angle calculated from align() " + requiredAngle);
        rotate(secondWay, requiredAngle, getCentroid(secondWay));
        MainApplication.getLayerManager().getEditLayer().invalidate();
    }

    public static void align(WaySegment roadSegment, WaySegment toRotateSegment) {
        double x1 = roadSegment.getFirstNode().getEastNorth().getX();
        double x2 = roadSegment.getSecondNode().getEastNorth().getX();
        double x3 = toRotateSegment.getFirstNode().getEastNorth().getX();
        double x4 = toRotateSegment.getSecondNode().getEastNorth().getX();
        double y1 = roadSegment.getFirstNode().getEastNorth().getY();
        double y2 = roadSegment.getSecondNode().getEastNorth().getY();
        double y3 = toRotateSegment.getFirstNode().getEastNorth().getY();
        double y4 = toRotateSegment.getSecondNode().getEastNorth().getY();
        
        double requiredAngle = Math.atan2(y2 - y1, x2 - x1)
                             - Math.atan2(y4 - y3, x4 - x3);
        Logging.info("Angle calculated from align() " + requiredAngle);
        
        requiredAngle = normalise(requiredAngle);
        
        rotate(toRotateSegment.way, requiredAngle, getCentroid(toRotateSegment.way));
        MainApplication.getLayerManager().getEditLayer().invalidate();
    }
    
    public static void align(WaySegment roadSegment, Way building){
        WaySegment closestSegment = ShapeMath.getClosestSegment(building, roadSegment);
        double x1 = roadSegment.getFirstNode().getEastNorth().getX();
        double x2 = roadSegment.getSecondNode().getEastNorth().getX();
        double x3 = closestSegment.getFirstNode().getEastNorth().getX();
        double x4 = closestSegment.getSecondNode().getEastNorth().getX();
        double y1 = roadSegment.getFirstNode().getEastNorth().getY();
        double y2 = roadSegment.getSecondNode().getEastNorth().getY();
        double y3 = closestSegment.getFirstNode().getEastNorth().getY();
        double y4 = closestSegment.getSecondNode().getEastNorth().getY();
        
        double requiredAngle = Math.atan2(y2 - y1, x2 - x1)
                             - Math.atan2(y4 - y3, x4 - x3);
        Logging.info("Angle calculated from align() " + requiredAngle);
        
        requiredAngle = normalise(requiredAngle);
        
        rotate(building, requiredAngle, getCentroid(building));
        MainApplication.getLayerManager().getEditLayer().invalidate();
    }
    
    public static double normalise(double a){

        while (a > Math.PI) {
            a -= 2 * Math.PI;
        }
        while (a <= -Math.PI) {
            a += 2 * Math.PI;
        }

        if (a > Math.PI / 2) {
            a -= Math.PI;
        } else if (a < -Math.PI / 2) {
            a += Math.PI;
        }
        return a;
    }
    
    public static void drawCenter() {

        OsmDataLayer currentLayer = MainApplication.getLayerManager().getEditLayer();
        Collection<Way> selectedWays = currentLayer.data.getSelectedWays();

        for (Way way : selectedWays) {
            EastNorth center = ShapeMath.getCentroid(way);
            MainApplication.getLayerManager().getEditLayer().data.addPrimitive(new Node(center));
        }
        MainApplication.getLayerManager().getEditLayer().invalidate();
    }
}
