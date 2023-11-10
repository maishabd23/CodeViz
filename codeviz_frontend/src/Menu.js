import React from 'react';

function Menu() {
    return (
        <div className='menu'>
            <p>&#123;Menu items&#125;</p>
            <form action="/api/search" method="get">
                <input type="text" name="search" placeholder="Search..."/>
                <input type="submit" value="Search"/>
            </form>
        </div>
    );
}

export default Menu;