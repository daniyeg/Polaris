'use client';

import { DashboardTabProps, UEData } from '../types';

function SignalChart({ data, metric }: { data: UEData[]; metric: keyof UEData }) {
  return (
    <div className="flex items-end h-48 bg-ocean-900 rounded p-2">
      {data.slice(0, 10).map((item, i) => (
        <div
          key={i}
          className="flex-1 mx-0.5 bg-primary"
          style={{ height: `${Math.abs(item[metric] as number)}%` }}
          title={`${item[metric]}`}
        ></div>
      ))}
    </div>
  );
}

export default function SignalsTab({ data }: DashboardTabProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div className="bg-ocean-800 rounded-lg p-4 shadow-md">
        <h3 className="font-medium mb-3">RSRP (dBm)</h3>
        <div className="h-64">
          <SignalChart data={data} metric="rsrp" />
        </div>
      </div>
      <div className="bg-ocean-800 rounded-lg p-4 shadow-md">
        <h3 className="font-medium mb-3">RSRQ (dB)</h3>
        <div className="h-64">
          <SignalChart data={data} metric="rsrq" />
        </div>
      </div>
      <div className="bg-ocean-800 rounded-lg p-4 shadow-md">
        <h3 className="font-medium mb-3">ECN0 (dB)</h3>
        <div className="h-64">
          <SignalChart data={data} metric="ecn0" />
        </div>
      </div>
      <div className="bg-ocean-800 rounded-lg p-4 shadow-md">
        <h3 className="font-medium mb-3">RXLEV</h3>
        <div className="h-64">
          <SignalChart data={data} metric="rxlev" />
        </div>
      </div>
    </div>
  );
}
