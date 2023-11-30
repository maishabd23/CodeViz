import React from 'react';

function Navbar() {
    return (
        <nav>
        <img src="/navbar-icon.png" alt='icon' className="nav--icon" />
        <div className='nav--main'>
            <h1 className="nav--title">CodeViz</h1>
            <h2 className="nav--description">A tool for code visualization</h2>
        </div>
        <div className='nav--dropdown'>
            {/*<h3 className='nav--dropdownItem'>&#123;Insert dropdown items&#125;</h3>*/}
        </div>
        </nav>
    );
}

export default Navbar;