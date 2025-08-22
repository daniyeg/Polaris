'use client';

import { useState, useMemo } from 'react';
import { DashboardTabProps, UEData } from '../types';

type SortConfig = {
  key: keyof UEData | null;
  direction: 'ascending' | 'descending';
};

export default function TableTab({ data }: DashboardTabProps) {
  const [sortConfig, setSortConfig] = useState<SortConfig>({ key: null, direction: 'ascending' });
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState<number | 'all'>(10);

  const totalPages = itemsPerPage === 'all' ? 1 : Math.ceil(data.length / itemsPerPage);

  const sortedData = useMemo(() => {
    if (!sortConfig.key) return data;

    return [...data].sort((a, b) => {
      if (a[sortConfig.key!] < b[sortConfig.key!]) {
        return sortConfig.direction === 'ascending' ? -1 : 1;
      }
      if (a[sortConfig.key!] > b[sortConfig.key!]) {
        return sortConfig.direction === 'ascending' ? 1 : -1;
      }
      return 0;
    });
  }, [data, sortConfig]);

  const currentItems = useMemo(() => {
    if (itemsPerPage === 'all') return sortedData;
    const startIndex = (currentPage - 1) * itemsPerPage;
    return sortedData.slice(startIndex, startIndex + itemsPerPage);
  }, [sortedData, currentPage, itemsPerPage]);

  const handleSort = (key: keyof UEData) => {
    let direction: 'ascending' | 'descending' = 'ascending';
    if (sortConfig.key === key && sortConfig.direction === 'ascending') {
      direction = 'descending';
    }
    setSortConfig({ key, direction });
    setCurrentPage(1);
  };

  const getSortIndicator = (key: keyof UEData) => {
    if (sortConfig.key !== key) return null;
    return sortConfig.direction === 'ascending' ? ' ▲' : ' ▼';
  };

  const handleItemsPerPageChange = (value: string) => {
    if (value === 'all') {
      setItemsPerPage('all');
    } else {
      setItemsPerPage(Number(value));
    }
    setCurrentPage(1);
  };

  const paginationButtons = [];
  const maxVisiblePages = 5;
  let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
  let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);

  if (endPage - startPage + 1 < maxVisiblePages) {
    startPage = Math.max(1, endPage - maxVisiblePages + 1);
  }

  for (let i = startPage; i <= endPage; i++) {
    paginationButtons.push(
      <button
        key={i}
        onClick={() => setCurrentPage(i)}
        className={`px-3 py-1 rounded ${currentPage === i ? 'bg-primary text-white' : 'bg-ocean-700 text-gray-300 hover:bg-ocean-600'}`}
      >
        {i}
      </button>
    );
  }

  return (
    <div className="h-full flex flex-col bg-ocean-800 rounded-lg shadow-md">
      <div className="flex justify-between items-center p-4">
        <h2 className="text-lg font-semibold">جدول داده‌ها</h2>
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-300">تعداد در صفحه:</span>
          <select
            value={itemsPerPage === 'all' ? 'all' : itemsPerPage}
            onChange={(e) => handleItemsPerPageChange(e.target.value)}
            className="bg-ocean-700 text-white rounded px-2 py-1"
          >
            <option value={10}>10</option>
            <option value={25}>25</option>
            <option value={100}>100</option>
            <option value="all">همه</option>
          </select>
        </div>
      </div>

      <div className="flex-1 overflow-auto">
        <table className="w-full text-sm">
          <thead className="sticky top-0 bg-ocean-900">
            <tr>
              {[
                'id',
                'phone_number',
                'timestamp',
                'lat',
                'lng',
                'gen',
                'tech',
                'plmn',
                'cid',
                'lac',
                'rac',
                'tac',
                'freq_band',
                'afrn',
                'freq',
                'rsrp',
                'rsrq',
                'ecno',
                'rxlev'
              ].map((key) => (
                <th
                  key={key}
                  className="py-3 px-2 text-center cursor-pointer hover:bg-ocean-700 whitespace-nowrap"
                  onClick={() => handleSort(key as keyof UEData)}
                >
                  {{
                    id: 'ID',
                    phone_number: 'شماره تلفن',
                    timestamp: 'زمان',
                    lat: 'عرض',
                    lng: 'طول',
                    gen: 'نسل',
                    tech: 'تکنولوژی',
                    plmn: 'PLMN',
                    cid: 'CID',
                    lac: 'LAC',
                    rac: 'RAC',
                    tac: 'TAC',
                    freq_band: 'باند فرکانس',
                    afrn: 'ARFCN',
                    freq: 'فرکانس',
                    rsrp: 'RSRP',
                    rsrq: 'RSRQ',
                    ecno: 'ECNO',
                    rxlev: 'RXLEV'
                  }[key]}
                  {getSortIndicator(key as keyof UEData)}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {currentItems.map((item) => (
              <tr key={item.id} className="border-b border-ocean-700 hover:bg-ocean-700">
                <td className="py-2 px-2 text-center">{item.id}</td>
                <td className="py-2 px-2 text-center">{item.phone_number}</td>
                <td className="py-2 px-2 text-center">{new Date(item.timestamp).toLocaleString('fa-IR')}</td>
                <td className="py-2 px-2 text-center">{item.lat.toFixed(6)}</td>
                <td className="py-2 px-2 text-center">{item.lng.toFixed(6)}</td>
                <td className="py-2 px-2 text-center">{item.gen}</td>
                <td className="py-2 px-2 text-center">{item.tech}</td>
                <td className="py-2 px-2 text-center">{item.plmn}</td>
                <td className="py-2 px-2 text-center">{item.cid}</td>
                <td className="py-2 px-2 text-center">{item.lac}</td>
                <td className="py-2 px-2 text-center">{item.rac}</td>
                <td className="py-2 px-2 text-center">{item.tac}</td>
                <td className="py-2 px-2 text-center">{item.freq_band}</td>
                <td className="py-2 px-2 text-center">{item.afrn}</td>
                <td className="py-2 px-2 text-center">{item.freq}</td>
                <td className="py-2 px-2 text-center">{item.rsrp}</td>
                <td className="py-2 px-2 text-center">{item.rsrq}</td>
                <td className="py-2 px-2 text-center">{item.ecno}</td>
                <td className="py-2 px-2 text-center">{item.rxlev}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="flex justify-between items-center p-4 border-t border-ocean-700">
        <div className="text-sm text-gray-300">
          نمایش {itemsPerPage === 'all'
            ? 1
            : ((currentPage - 1) * itemsPerPage) + 1
          } تا {itemsPerPage === 'all'
            ? sortedData.length
            : Math.min(currentPage * itemsPerPage, sortedData.length)
          } از {sortedData.length} مورد
        </div>
        <div className="flex gap-1">
          <button
            onClick={() => setCurrentPage(1)}
            disabled={currentPage === 1}
            className="px-3 py-1 rounded bg-ocean-700 text-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-ocean-600"
          >
            اولین
          </button>
          <button
            onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
            disabled={currentPage === 1}
            className="px-3 py-1 rounded bg-ocean-700 text-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-ocean-600"
          >
            قبلی
          </button>

          {startPage > 1 && (
            <span className="px-2 py-1">...</span>
          )}

          {paginationButtons}

          {endPage < totalPages && (
            <span className="px-2 py-1">...</span>
          )}

          <button
            onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
            disabled={currentPage === totalPages}
            className="px-3 py-1 rounded bg-ocean-700 text-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-ocean-600"
          >
            بعدی
          </button>
          <button
            onClick={() => setCurrentPage(totalPages)}
            disabled={currentPage === totalPages}
            className="px-3 py-1 rounded bg-ocean-700 text-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-ocean-600"
          >
            آخرین
          </button>
        </div>
      </div>
    </div>
  );
}
