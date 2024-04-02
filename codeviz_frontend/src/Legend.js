import React, {useState} from 'react';
import legendItems from './LegendItems';

import './Legend.css';
import {selectedItems, setSelectedItems} from  "./GraphViz";

function Legend() {
    const [selectAll, setSelectAll] = useState(true);

    const handleCheckboxChange = (category) => {
        if (selectedItems.includes(category)) {
            console.log(`Legend option "${category}" un-pressed`);
            setSelectedItems(selectedItems.filter(item => item !== category));
        } else {
            console.log(`Legend option "${category}" pressed`);
            setSelectedItems([...selectedItems, category]);
        }
    };

    const handleSelectAllChange = () => {
        if (selectAll) {
            setSelectedItems([]);
        } else {
            const allCategories = legendItems.map(item => item.color);
            setSelectedItems(allCategories);
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
                        checked={selectedItems.includes(item.color)}
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
