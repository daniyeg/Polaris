'use client';

import { useEffect, useRef, useState } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { FaCog } from 'react-icons/fa';
import { UEData } from './types';

interface MapComponentProps {
  data: UEData[];
}

interface LevelConfig {
  min: number;
  max: number;
  color: string;
}

interface MapConfig {
  measure: 'rsrp' | 'rsrq' | 'ecno' | 'rxlev';
  levels: LevelConfig[];
}

const DEFAULT_CONFIG: MapConfig = {
  measure: 'rsrp',
  levels: [
    { min: -85, max: Infinity, color: '#4ade80' },
    { min: -95, max: -85, color: '#fbbf24' },
    { min: -105, max: -95, color: '#f97316' },
    { min: -Infinity, max: -105, color: '#ef4444' }
  ]
};

export default function MapComponent({ data }: MapComponentProps) {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstance = useRef<L.Map | null>(null);
  const markersRef = useRef<L.CircleMarker[]>([]);
  const [showConfig, setShowConfig] = useState(false);
  const [config, setConfig] = useState<MapConfig>(DEFAULT_CONFIG);

  useEffect(() => {
    if (!mapRef.current) return;

    mapInstance.current = L.map(mapRef.current).setView([35.6892, 51.3890], 12);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(mapInstance.current);

    return () => {
      mapInstance.current?.remove();
    };
  }, []);

  useEffect(() => {
    if (!mapInstance.current || !data.length) return;

    markersRef.current.forEach(marker => marker.remove());
    markersRef.current = [];

    data.forEach(point => {
      const value = point[config.measure];
      const marker = L.circleMarker([point.lat, point.lng], {
        radius: 8,
        fillColor: value === null || value === undefined ? '#000000' : getColorForValue(value),
        color: '#fff',
        weight: 1,
        opacity: 1,
        fillOpacity: 0.8
      }).addTo(mapInstance.current!);

      marker.bindPopup(`
        <div class="text-sm">
          <strong>موقعیت:</strong> ${point.lat.toFixed(4)}, ${point.lng.toFixed(4)}<br>
          <strong>زمان:</strong> ${new Date(point.timestamp).toLocaleString()}<br>
          <strong>نسل:</strong> ${point.gen}<br>
          <strong>تکنولوژی:</strong> ${point.tech}<br>
          <strong>RSRP:</strong> ${point.rsrp} dBm<br>
          <strong>RSRQ:</strong> ${point.rsrq} dB<br>
          ${point.ecno ? `<strong>ECN0:</strong> ${point.ecno} dB<br>` : ''}
          ${point.rxlev ? `<strong>RXLEV:</strong> ${point.rxlev} dBm<br>` : ''}
        </div>
      `);

      markersRef.current.push(marker);
    });

    if (markersRef.current.length > 0) {
      const group = L.featureGroup(markersRef.current);
      mapInstance.current.fitBounds(group.getBounds());
    }
  }, [data, config]);

  const getColorForValue = (value: number): string => {
    for (const level of config.levels) {
      if (value >= level.min && value <= level.max) {
        return level.color;
      }
    }
    return '#000000';
  };

  const updateLevel = (index: number, field: keyof LevelConfig, value: string | number) => {
    const newLevels = [...config.levels];
    newLevels[index] = {
      ...newLevels[index],
      [field]: typeof value === 'string' && field === 'color' ? value : Number(value)
    };
    setConfig({ ...config, levels: newLevels });
  };

  const addLevel = () => {
    setConfig({
      ...config,
      levels: [...config.levels, { min: -120, max: -110, color: '#cccccc' }]
    });
  };

  const removeLevel = (index: number) => {
    if (config.levels.length <= 1) return;
    const newLevels = config.levels.filter((_, i) => i !== index);
    setConfig({ ...config, levels: newLevels });
  };

  const restoreDefaults = () => {
    setConfig(DEFAULT_CONFIG);
  };

  return (
    <div className="w-full h-full rounded-md relative">
      <div
        className="absolute top-2 right-2 z-[1000] bg-white p-2 rounded-md shadow-md cursor-pointer hover:bg-gray-100"
        onClick={() => setShowConfig(!showConfig)}
      >
        <FaCog className="text-gray-700" />
      </div>

      {showConfig && (
        <div className="absolute top-12 right-2 z-[1000] bg-white p-4 rounded-md shadow-md w-80 max-h-[60vh] flex flex-col">
          <h3 className="font-bold mb-3 text-black">تنظیمات نمایش نقشه</h3>

          <div className="mb-4">
            <label className="block mb-1 text-black">معیار اندازه‌گیری:</label>
            <select
              className="w-full p-2 border rounded text-black"
              value={config.measure}
              onChange={(e) => setConfig({ ...config, measure: e.target.value as any })}
            >
              <option value="rsrp" className="text-black">RSRP</option>
              <option value="rsrq" className="text-black">RSRQ</option>
              <option value="ecno" className="text-black">ECN0</option>
              <option value="rxlev" className="text-black">RXLEV</option>
            </select>
          </div>

          <div className="mb-4 flex-grow overflow-y-auto">
            <div className="flex justify-between items-center mb-2">
              <label className="text-black">سطوح و رنگ‌ها:</label>
              <button
                className="bg-ocean-500 text-white px-2 py-1 rounded text-sm"
                onClick={addLevel}
              >
                افزودن سطح
              </button>
            </div>

            {config.levels.map((level, index) => (
              <div key={index} className="flex items-center mb-2 gap-2">
                <input
                  type="number"
                  className="w-20 p-1 border rounded text-black"
                  value={level.min === -Infinity ? '' : level.min}
                  placeholder="حداقل"
                  onChange={(e) => updateLevel(index, 'min', e.target.value || -Infinity)}
                />
                <span className="text-black">تا</span>
                <input
                  type="number"
                  className="w-20 p-1 border rounded text-black"
                  value={level.max === Infinity ? '' : level.max}
                  placeholder="حداکثر"
                  onChange={(e) => updateLevel(index, 'max', e.target.value || Infinity)}
                />
                <input
                  type="color"
                  value={level.color}
                  onChange={(e) => updateLevel(index, 'color', e.target.value)}
                  className="w-8 h-8"
                />
                <button
                  className="text-red-500 hover:text-red-700 text-sm"
                  onClick={() => removeLevel(index)}
                  disabled={config.levels.length <= 1}
                >
                  حذف
                </button>
              </div>
            ))}
          </div>

          <div className="flex gap-2 mt-auto pt-1">
            <button
              className="flex-1 bg-teal-800 text-white px-1 py-1 rounded"
              onClick={restoreDefaults}
            >
              بازگشت به پیش‌فرض
            </button>
            <button
              className="flex-1 bg-teal-500 text-white px-1 py-1 rounded"
              onClick={() => setShowConfig(false)}
            >
              اعمال تغییرات
            </button>
          </div>
        </div>
      )}

      <div ref={mapRef} className="w-full h-full rounded-md" />
    </div>
  );
}
