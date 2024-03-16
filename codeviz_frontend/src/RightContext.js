import React from "react";
import './RightContext.css';

//define a functional component for the right-click context menu
function RightContext() {
    //state variables
    const [context, setContext] = React.useState(false);
    const [xyPosition, setxyPosition] = React.useState({ x: 0, y: 0 });
    const [level, setLevel] = React.useState('Class');

    fetch('/api/getCurrentLevel')
        .then((response) => response.json())
        .then((responseData) => {
            setLevel(responseData.string);
        });

    //event handler for showing the context menu
    const showNav = (event) => {
        event.preventDefault();
        setContext(false);
        const positionChange = {
            x: event.pageX,
            y: event.pageY,
        };
        setxyPosition(positionChange);
        setContext(true);
    };

    //event handler for hiding the context menu
    const hideContext = (event) => {
        setContext(false);
    };

    //function to set the chosen menu option
    const handleNodeOption = (chosenNodeApi) => {
        fetch('/api/getHoveredNodeString')
            .then((response) => response.json())
            .then((responseData) => {
                let hoveredNode = responseData.string;
                if (hoveredNode != null) {
                    if (chosenNodeApi === 'generateInnerGraph') {
                        fetch('/api/generateInnerGraph?nodeName=' + hoveredNode.toString());
                    } else {
                        fetch('/api/' + chosenNodeApi + '?nodeName=' + hoveredNode.toString())
                            .then((response) => response.json())
                            .then((responseData) => {
                                document.getElementById("nodeDetails").innerHTML = responseData.string;
                            });
                    }
                } else {
                    console.log("ERROR, hoveredNode is " + hoveredNode);
                }
            });

    };

    //JavaScript XML (JSX) returned by the component
    return (
        <>
            <div
                className="graphDisplay--image"
                onContextMenu={showNav}
                onClick={hideContext}
            >
                {context && (
                    <div
                        style={{ top: xyPosition.y, left: xyPosition.x }}
                        className="rightClick"
                    >
                        <div className="menuElement" onClick={() => handleNodeOption("getNodeDetails")}>
                            View Node Details
                        </div>
                        {level !== 'Package' && (
                            <div className="menuElement" onClick={() => handleNodeOption("getComplexityDetails")}>
                            View Complexity Metrics
                            </div>
                        )}
                        {level !== 'Method' && (
                            <div className="menuElement" onClick={() => handleNodeOption("generateInnerGraph")}>
                                Generate Inner Graph
                            </div>
                        )}
                    </div>
                )}
            </div>
        </>
    );
}
export default RightContext;