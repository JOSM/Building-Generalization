// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.buildinggeneralization;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.tools.Geometry;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * @author ignacio_palermo
 *
 */
public class BuildingGeneralizationAction extends JosmAction {
    private List<Double> editedAngles;

    double epsilon = Math.pow(10, -3);

    /**
     * Constructs a new {@code BuildingGeneralizationAction}.
     */
    public BuildingGeneralizationAction() {
        super(tr("Building Generalization"), "dialogs/rsz_3untitled.png",
                tr("Beginners draw outlines often very inaccurate. Such buildings shall be converted to rectangular ones. Angles between 84-96 degrees are converted to 90 degrees."),
                Shortcut.registerShortcut("menu:buildinggeneralization", tr("Menu: {0}", tr("Building Generalization")), KeyEvent.VK_G, Shortcut.ALT_CTRL),
                false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (MainApplication.getMap() != null) {
            Collection<Way> ways = getLayerManager().getEditLayer().data.getWays();
            generalization(ways);
            for (Way way : ways) {
                if (way.isClosed()) {
                    List<Node> currentWayNodes = way.getNodes();
                    Logging.info("New angles are :");
                    for (int i = 0; i < currentWayNodes.size() - 1; i++) {
                        if (i + 1 >= currentWayNodes.size() - 1) {
                            double angle = Geometry.getCornerAngle(
                                    currentWayNodes.get(i).getEastNorth(),
                                    currentWayNodes.get((i + 1) - (currentWayNodes.size() - 1)).getEastNorth(),
                                    currentWayNodes.get((i + 2) - (currentWayNodes.size() - 1)).getEastNorth());
                            Logging.info(Double.toString(Math.toDegrees(angle)));
                        } else {
                            double angle = Geometry.getCornerAngle(
                                    currentWayNodes.get(i).getEastNorth(),
                                    currentWayNodes.get(i + 1).getEastNorth(),
                                    currentWayNodes.get(i + 2).getEastNorth());
                            Logging.info(Double.toString(Math.toDegrees(angle)));
                        }
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, tr("There is no frame loaded !"), tr("Alert Message"), JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * We will modify only the angles with the value between 84-96 degrees
     * 
     * @param way way to process
     */
    public void proceedGeneralization(Way way) {

        editedAngles = new ArrayList<>();
        List<Node> currentWayNodes = way.getNodes();
        System.out.println(way.getNodes().size());
        for (int i = 0; i < currentWayNodes.size() - 1; i++) {
            if (i + 1 >= currentWayNodes.size() - 1) {
                double angle = Geometry.getCornerAngle(currentWayNodes.get(i).getEastNorth(),
                        currentWayNodes.get((i + 1) - (currentWayNodes.size() - 1)).getEastNorth(),
                        currentWayNodes.get((i + 2) - (currentWayNodes.size() - 1)).getEastNorth());
                System.out.println(Math.toDegrees(angle));
                if (Math.abs(Math.toDegrees(angle)) >= 84
                        && Math.abs(Math.toDegrees(angle)) <= 96) {
                    if (Math.abs(90 - (Math.abs(Math.toDegrees(angle)))) > epsilon) {
                        if (Math.toDegrees(angle) < 0) {
                            Logging.info("Rotation Angle is :" + (-90 - Math.toDegrees(angle)));
                            executeRotation(Math.toRadians(-1.0 * (-90 - Math.toDegrees(angle))),
                                    new WaySegment(way, (i + 1) - (currentWayNodes.size() - 1)));
                            editedAngles.add(Math.abs(Math.toDegrees(Geometry.getCornerAngle(
                                                    currentWayNodes.get(i).getEastNorth(),
                                                    currentWayNodes.get((i + 1) - (currentWayNodes.size() - 1)).getEastNorth(),
                                                    currentWayNodes.get((i + 2) - (currentWayNodes.size() - 1)).getEastNorth()))));
                        } else {
                            Logging.info("Rotation Angle is :" + (90 - Math.toDegrees(angle)));
                            executeRotation(Math.toRadians(-1.0 * (90 - Math.toDegrees(angle))),
                                    new WaySegment(way, (i + 1) - (currentWayNodes.size() - 1)));
                            editedAngles.add(Math.abs(Math.toDegrees(Geometry.getCornerAngle(
                                                    currentWayNodes.get(i).getEastNorth(),
                                                    currentWayNodes.get((i + 1) - (currentWayNodes.size() - 1)).getEastNorth(), 
                                                    currentWayNodes.get((i + 2) - (currentWayNodes.size() - 1)).getEastNorth()))));
                        }
                    }
                }
            } else {
                double angle = Geometry.getCornerAngle(
                        currentWayNodes.get(i).getEastNorth(),
                        currentWayNodes.get(i + 1).getEastNorth(),
                        currentWayNodes.get(i + 2).getEastNorth());
                System.out.println(Math.toDegrees(angle));
                if (Math.abs(Math.toDegrees(angle)) >= 84 && Math.abs(Math.toDegrees(angle)) <= 96) {
                    if (Math.abs(90 - (Math.abs(Math.toDegrees(angle)))) > epsilon) {
                        if (Math.toDegrees(angle) < 0) {
                            Logging.info("Rotation Angle is :" + (-90 - Math.toDegrees(angle)));
                            executeRotation(Math.toRadians(-1.0 * (-90 - Math.toDegrees(angle))), new WaySegment(way, (i + 1)));
                            editedAngles.add(Math.abs(Math.toDegrees(Geometry.getCornerAngle(
                                            currentWayNodes.get(i).getEastNorth(),
                                            currentWayNodes.get(i + 1).getEastNorth(),
                                            currentWayNodes.get(i + 2).getEastNorth()))));
                        } else {
                            Logging.info("Rotation Angle is :" + (90 - Math.toDegrees(angle)));
                            executeRotation(Math.toRadians(-1.0 * (90 - Math.toDegrees(angle))), new WaySegment(way, (i + 1)));
                            editedAngles.add(Math.abs(Math.toDegrees(Geometry.getCornerAngle(
                                            currentWayNodes.get(i).getEastNorth(),
                                            currentWayNodes.get(i + 1).getEastNorth(),
                                            currentWayNodes.get(i + 2).getEastNorth()))));
                        }
                    }
                }
            }
        }
    }

    public WaySegment findSegment(Way closedWay) {

        int nodeIndex = Integer.MAX_VALUE;
        double min = Double.MAX_VALUE;
        Collection<Way> ways = getLayerManager().getEditLayer().data.getWays();
        Collection<Way> roads = new ArrayList<>();
        Way minWay = new Way();
        for (Way w : ways)
            if (!w.isClosed())
                roads.add(w);
        EastNorth buildingCenter = ShapeMath.getCentroid(closedWay);
        for (Way road : roads) {
            for (int i = 0; i < road.getNodes().size(); i++) {
                double dist = Point.distance(
                        new Point(buildingCenter.east(), buildingCenter.north()),
                        new Point(road.getNode(i).getEastNorth().east(),
                                  road.getNode(i).getEastNorth().north()));
                if (dist < min) {
                    min = dist;
                    minWay = road;
                    nodeIndex = i;
                    ShapeMath.containingWay = minWay;
                }
            }
        }
        if (!roads.isEmpty()) {
            if (nodeIndex == 0)
                return new WaySegment(minWay, nodeIndex);
            else if (nodeIndex == minWay.getNodes().size() - 1)
                return new WaySegment(minWay, nodeIndex - 1);
            if (!(nodeIndex == 0 || nodeIndex == minWay.getNodes().size() - 1)) {
                double firstDist = Point.distance(
                        new Point(minWay.getNode(nodeIndex - 1).getEastNorth().east(),
                                  minWay.getNode(nodeIndex - 1).getEastNorth().north()),
                        new Point(minWay.getNode(nodeIndex).getEastNorth().east(),
                                  minWay.getNode(nodeIndex).getEastNorth().north()),
                        new Point(buildingCenter.east(), buildingCenter.north()));
                double secondDist = Point.distance(
                        new Point(minWay.getNode(nodeIndex).getEastNorth().east(),
                                  minWay.getNode(nodeIndex).getEastNorth().north()),
                        new Point(minWay.getNode(nodeIndex + 1).getEastNorth().east(),
                                  minWay.getNode(nodeIndex + 1).getEastNorth().north()),
                        new Point(buildingCenter.east(), buildingCenter.north()));
                if (firstDist < secondDist)
                    return new WaySegment(minWay, nodeIndex - 1);
                else
                    return new WaySegment(minWay, nodeIndex);
            }
        }
        return null;
    }

    public void generalization(Collection<Way> ways) {
        for (Way way : ways) {
            if (way.isClosed()) {
                proceedGeneralization(way);
                if (!editedAngles.isEmpty()) {
                    proceedGeneralization(way);
                }
                for (int i = 0; i < 3; i++) {
                    WaySegment waySegm = findSegment(way);
                    if (waySegm != null) {
                        WaySegment buildingSegment = ShapeMath.getClosestSegment(way, waySegm);
                        if (!(buildingSegment.getFirstNode().getEastNorth().east() < ShapeMath.containingWay.getNode(0).getEastNorth().east()
                           || buildingSegment.getSecondNode().getEastNorth().east() > ShapeMath.containingWay.getNode(
                                                ShapeMath.containingWay.getNodes().size() - 1).getEastNorth().east()))
                            ShapeMath.align(waySegm, buildingSegment);
                    }
                }
            }
        }
    }

    public boolean allAnglesInInterval84_96(Way way) {

        List<Node> currentWayNodes = way.getNodes();
        for (int i = 0; i < currentWayNodes.size() - 1; i++) {
            if (i + 1 >= currentWayNodes.size() - 1) {
                double angle = Geometry.getCornerAngle(
                        currentWayNodes.get(i).getEastNorth(),
                        currentWayNodes.get((i + 1) - (currentWayNodes.size() - 1)).getEastNorth(),
                        currentWayNodes.get((i + 2) - (currentWayNodes.size() - 1)).getEastNorth());
                if (Math.abs(Math.toDegrees(angle)) >= 84 && Math.abs(Math.toDegrees(angle)) <= 96) {
                    return true;
                }
            } else {
                double angle = Geometry.getCornerAngle(
                        currentWayNodes.get(i).getEastNorth(),
                        currentWayNodes.get(i + 1).getEastNorth(),
                        currentWayNodes.get(i + 2).getEastNorth());
                if (Math.abs(Math.toDegrees(angle)) >= 84 && Math.abs(Math.toDegrees(angle)) <= 96) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean allAnglesAreCorrected(List<Double> angles) {
        for (Double ang : angles)
            if (!(ang >= 89 && ang <= 91))
                return false;
        return true;
    }

    public void executeRotation(double angle, WaySegment segment) {
        double centerX = segment.getFirstNode().getEastNorth().east();
        double centerY = segment.getFirstNode().getEastNorth().north();
        double x = segment.getSecondNode().getEastNorth().east();
        double y = segment.getSecondNode().getEastNorth().north();

        double newX = centerX + (x - centerX) * Math.cos(angle) - (y - centerY) * Math.sin(angle);
        double newY = centerY + (x - centerX) * Math.sin(angle) + (y - centerY) * Math.cos(angle);
        EastNorth eastNorth = new EastNorth(newX, newY);
        segment.getSecondNode().setEastNorth(eastNorth);

        MainApplication.getLayerManager().getEditLayer().invalidate();
    }
}
