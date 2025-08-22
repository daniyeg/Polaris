'use client';

import dynamic from 'next/dynamic';
import { DashboardTabProps } from '../types';

const MapWithNoSSR = dynamic(() => import('../MapComponent'), {
  ssr: false,
  loading: () => <div className="h-120 flex items-center justify-center">Loading map...</div>
});

export default function MapTab({ data, testData }: DashboardTabProps) {
  return (
    <div className="h-130">
      <MapWithNoSSR data={data} />
    </div>
  );
}
