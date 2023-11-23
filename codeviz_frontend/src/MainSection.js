import React from 'react';
import GraphViz from './GraphViz';
import Menu from './Menu';

function MainSection() {
    return (
        <div className='main'>
            <Menu />
            <GraphViz />
            {/* <Graph /> */}
        </div>
    );
}

export default MainSection;