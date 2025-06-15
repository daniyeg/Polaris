'use client';

import dynamic from 'next/dynamic';
import { DashboardTabProps } from '../types';

const MapWithNoSSR = dynamic(() => import('../MapComponent'), {
  ssr: false,
  loading: () => <div className="h-80 flex items-center justify-center">Loading map...</div>
});

export default function MapTab({ data }: DashboardTabProps) {
  return (
    <div className="bg-ocean-800 rounded-lg p-4 shadow-md h-96">
      <MapWithNoSSR data={data} />
    </div>
  );
}
