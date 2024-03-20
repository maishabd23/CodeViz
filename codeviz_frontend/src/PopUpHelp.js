import React from "react";
import './PopUpHelp.css';

import {setContext} from './RightContext'; // read-only

function PopUpHelp(props) {

    if (setContext != null) {
        setContext(false); // remove right click menu if it's visible
    }

    return (props.trigger) ? (
        <div className="popup-help">
            <div className="popup-help-inner">
                <button className="close-btn" onClick={() => props.setTrigger(false)}>Close</button>
                <h1>Documentation</h1>

                <h3>How to Generate Graph:</h3>
                <p>
                    Enter a repo url that corresponds to a public repo in GitHub.
                    CodeViz only supports the visualization of Java projects.
                </p>
                <p>
                    While using the system, a new project can be visualized by entering a different repo url.
                </p>

                <h3>Switch Level:</h3>
                <p>
                    There are three possible graph levels: package, class, and method.
                    These levels represent the 3 levels in an object-oriented project.
                </p>

                <h3>Graph Information:</h3>
                <p>Each node represents an entity at that level of the graph.
                    For example in the class view, each node represents a class in that project.
                    The size of the node depends on the number of items within the node.
                    For example, a class with many fields and methods will be larger than a node
                    with less fields and methods.
                </p>
                <p>
                    Each edge represent a connection between nodes.
                    For example in the class view, a connection means that one class references another class,
                    either in it's fields or methods.
                    The thicker a connection, the stronger two nodes are related.
                </p>

                <h4>Node Options:</h4>
                <p>
                    Hovering on a node will highlight that node and its immediate connections,
                    and grey out all other nodes.
                </p>
                <p>
                    Clicking on a node (either right click or left click) will provide options related to the node.
                    The possible options are:
                </p>
                <ol>
                    <li><b>View Node Details:</b> View the details of the node.
                        <ol>
                            <li>Package Node: The classes within the package</li>
                            <li>Class Node: The package, super class, fields, and methods</li>
                            <li>Method Node: The class, arguments and return type</li>
                        </ol>
                    </li>
                    <li><b>View Complexity Metrics:</b> View complexity metrics such as cyclomatic complexity and lines of code.
                        Only applicable for Class and Method View.
                    </li>
                    <li><b>Generate Inner Graph:</b> View a filtered version of the graph, of all the nodes within that node.
                        Only applicable for Package and Class View.
                        <ol>
                            <li>Package Node: View filtered graph of all classes in the package</li>
                            <li>Class Node: View filtered graph of all methods in the class</li>
                        </ol>
                    </li>
                </ol>

                <h4>Git History Graph:</h4>
                <p>
                    On the Class View, there is the option to generate a Git History Graph.
                    On this graph, each connection represents two classes that have dependencies between each other
                    and also share git commits together.
                </p>
                <p>
                    The thickness of the edges are calculated using association rule mining, which calculates the confidence
                    between two classes.
                </p>
                <ul>
                    <li>pA = # times class A was changed / # times a class was changed</li>
                    <li>pAorB = # times class A or class B was changed / # times a class was changed</li>
                    <li>confidence = pAorB / pA</li>
                </ul>
                <p>
                    When hovering on an edge, details on most recent commit between the two classes
                    will be displayed on the screen.
                </p>

                <h3>Search</h3>
                <p>
                    A search query can be entered in the search bar, and corresponding nodes will become
                    highlighted in a bright yellow on the screen.
                    Note that searching is case sensitive.
                </p>

                <h3>Label Threshold</h3>
                <p>
                    The label threshold slider can be used to filter the amount of nodes that have their label displayed,
                    based on the node sizes. A threshold of 0 will show all node labels. As the threshold increases,
                    less of the smaller nodes will have their labels displayed.
                </p>
                <p>
                    An advanced option is to modify the threshold settings (min, max, step).
                    Min and Max control the range of the slider, and step controls the increment each time the slider is moved.
                </p>

                {props.children}
            </div>
        </div>
    ) : "";
}

export default PopUpHelp;