import React from 'react';
import Graph from './Graph';
import Menu from './Menu';

function MainSection() {
    return (
        <div className='main'>
            <Menu />
            <Graph />
        </div>
    );
}

export default MainSection;