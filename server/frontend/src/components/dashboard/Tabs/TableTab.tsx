'use client';

import { DashboardTabProps } from '../types';

export default function TableTab({ data }: DashboardTabProps) {
  return (
    <div className="bg-ocean-800 rounded-lg p-4 shadow-md">
      <h2 className="text-lg font-semibold mb-4">جدول داده‌ها</h2>
      <div className="overflow-x-auto">
        <table className="min-w-full text-sm">
          <thead>
            <tr className="border-b border-ocean-700">
              <th className="py-2 px-4 text-right">زمان</th>
              <th className="py-2 px-4 text-right">مکان</th>
              <th className="py-2 px-4 text-right">نسل</th>
              <th className="py-2 px-4 text-right">تکنولوژی</th>
              <th className="py-2 px-4 text-right">PLMN</th>
              <th className="py-2 px-4 text-right">RSRP</th>
              <th className="py-2 px-4 text-right">RSRQ</th>
            </tr>
          </thead>
          <tbody>
            {data.slice(0, 10).map((item, index) => (
              <tr key={index} className="border-b border-ocean-700 hover:bg-ocean-700">
                <td className="py-2 px-4">{new Date(item.timestamp).toLocaleTimeString()}</td>
                <td className="py-2 px-4">{item.lat.toFixed(4)}, {item.lng.toFixed(4)}</td>
                <td className="py-2 px-4">{item.gen}</td>
                <td className="py-2 px-4">{item.tech}</td>
                <td className="py-2 px-4">{item.plmn}</td>
                <td className="py-2 px-4">{item.rsrp}</td>
                <td className="py-2 px-4">{item.rsrq}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
