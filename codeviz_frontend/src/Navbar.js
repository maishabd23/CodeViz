import React from 'react';
import PopUpHelp from "./PopUpHelp";

function Navbar() {
    const [popUpMenu, setPopUpMenu] = React.useState(false);

    return (
        <nav>
        <img src="/navbar-icon.png" alt='icon' className="nav--icon" />
        <div className='nav--main'>
            <font color="white">
                <h1 className="nav--title">CodeViz</h1>
                <h2 className="nav--description">A tool for code visualization</h2>
            </font>
            {/*TODO find proper title and location for help menu*/}
            <button onClick={() => setPopUpMenu(true)}>Documentation</button>
            <PopUpHelp trigger={popUpMenu} setTrigger={setPopUpMenu}></PopUpHelp>
        </div>
        <div className='nav--dropdown'>
            {/*<h3 className='nav--dropdownItem'>&#123;Insert dropdown items&#125;</h3>*/}
        </div>
        </nav>
    );
}

export default Navbar;