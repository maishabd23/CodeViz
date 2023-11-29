import React, {useEffect} from 'react';

function Menu() {

    useEffect(() => {
        const fetchData = async () => {
            const search = document.getElementById("myInput");

            search.addEventListener("search", myFunction);
            function myFunction() {
                var x = document.getElementById("myInput");
                document.getElementById("printSearch").innerHTML = "Searching for: " + x.value;
                fetch('/api/viewGraphLevel?searchValue=' + x.value);
            }

        };

        fetchData();
    }, []);

    return (
        <div className='menu'>
            <p>Menu</p>
            <input type="search" id="myInput"/>{/*TODO - add submit button?*/}
            <p id="printSearch"></p>

        </div>
    );
}

export default Menu;