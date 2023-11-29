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