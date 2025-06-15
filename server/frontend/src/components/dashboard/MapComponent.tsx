'use client';

import { useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

interface MapComponentProps {
  data: {
    lat: number;
    lng: number;
    rsrp: number;
    timestamp: number;
    gen: string;
    tech: string;
    rsrq: number;
  }[];
}

export default function MapComponent({ data }: MapComponentProps) {
  const mapRef = useRef<HTMLDivElement>(null);
  const mapInstance = useRef<L.Map | null>(null);
  const markersRef = useRef<L.CircleMarker[]>([]);

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
      const marker = L.circleMarker([point.lat, point.lng], {
        radius: 8,
        fillColor: getColorForSignal(point.rsrp),
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
          <strong>RSRQ:</strong> ${point.rsrq} dB
        </div>
      `);

      markersRef.current.push(marker);
    });

    if (markersRef.current.length > 0) {
      const group = L.featureGroup(markersRef.current);
      mapInstance.current.fitBounds(group.getBounds());
    }
  }, [data]);

  function getColorForSignal(rsrp: number): string {
    if (rsrp >= -85) return '#4ade80';
    if (rsrp >= -95) return '#fbbf24';
    if (rsrp >= -105) return '#f97316';
    return '#ef4444';
  }

  return <div ref={mapRef} className="w-full h-full rounded-md" />;
}
