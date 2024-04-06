import React, {useState} from 'react';
import legendItems from './LegendItems';

import './Legend.css';
import {selectedColours, setSelectedColours} from  "./GraphViz";

function Legend() {
    const [selectAll, setSelectAll] = useState(false);

    const handleCheckboxChange = (category) => {
        if (selectedColours.includes(category)) {
            console.log(`Legend option "${category}" un-pressed`);
            setSelectedColours(selectedColours.filter(item => item !== category));
        } else {
            console.log(`Legend option "${category}" pressed`);
            setSelectedColours([...selectedColours, category]);
        }
    };

    const handleSelectAllChange = () => {
        if (selectAll) {
            setSelectedColours([]);
        } else {
            const allCategories = legendItems.map(item => item.color);
            setSelectedColours(allCategories);
        }
        setSelectAll(!selectAll);
    };

    return (
        <div id="legend">
            <h3>Legend</h3>
            <div>
                <input
                    type="checkbox"
                    checked={selectAll}
                    onChange={handleSelectAllChange}
                />
                <label>Select/Deselect All</label>
            </div>
            {legendItems.map(item => (
                <div key={item.category} className="legend-item">
                    <input
                        type="checkbox"
                        checked={selectedColours.includes(item.color)}
                        onChange={() => handleCheckboxChange(item.color)}
                    />
                    <span className="legend-color" style={{ backgroundColor: item.color }}></span>
                    <span className="legend-text">{item.category}</span>
                </div>
            ))}
        </div>
    );

}

export default Legend;
