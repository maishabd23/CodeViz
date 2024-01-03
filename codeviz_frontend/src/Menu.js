import React, {useEffect} from 'react';

function Menu() {

    useEffect(() => {
        const fetchData = async () => {

            const packageView = document.getElementById("package-view");
            const classView = document.getElementById("class-view");
            const methodView = document.getElementById("method-view");

            // Bind view level buttons
            packageView.addEventListener("click", () => {
                fetch('/api/viewGraphLevel?level=PACKAGE');
            });
            classView.addEventListener("click", () => {
                fetch('/api/viewGraphLevel?level=CLASS');
            });
            methodView.addEventListener("click", () => {
                fetch('/api/viewGraphLevel?level=METHOD');
            });

            const search = document.getElementById("searchInput");
            search.addEventListener("search", mySearchFunction);
            function mySearchFunction() {
                var x = document.getElementById("searchInput");
                document.getElementById("printSearch").innerHTML = "Searching for: " + x.value;
                fetch('/api/viewGraphLevel?searchValue=' + x.value);
            }

            const detailedSearch = document.getElementById("detailedSearchInput");
            detailedSearch.addEventListener("search", myDetailedSearchFunction);
            function myDetailedSearchFunction() {
                var x = document.getElementById("detailedSearchInput");
                document.getElementById("printSearch").innerHTML = "Searching for: " + x.value;
                fetch('/api/viewGraphLevel?detailed=true&searchValue=' + x.value);
            }

        };

        fetchData();
    }, []);

    return (
        <div className='menu'>
            <h2>Menu</h2>
            {/*TODO - add submit button?*/}
            {/*TODO - remove duplicate search bars and use dropdown instead - either simple or detailed search*/}
            <input type="search" id="searchInput" placeholder="Simple Search..."/>
            <input type="search" id="detailedSearchInput" placeholder="Detailed Search..."/>
            <p id="printSearch"></p>
            <table className="center">
                <tbody>
                <tr>
                    <td>
                        <div className="input"><label htmlFor="package-view"></label><button id="package-view">Package</button></div>
                    </td>
                    <td>
                        <div className="input"><label htmlFor="class-view"></label><button id="class-view">Class</button></div>
                    </td>
                    <td>
                        <div className="input"><label htmlFor="method-view"></label><button id="method-view">Method</button></div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    );
}

export default Menu;