'use client';

import { useState, useMemo } from 'react';
import { DashboardTabProps, UEData, TestData } from '../types';

type SortConfig = {
  key: string | null;
  direction: 'ascending' | 'descending';
};

type TableItem = UEData | TestData;

export default function TableTab({ data, testData }: DashboardTabProps) {
  const [activeSubTab, setActiveSubTab] = useState<string>('cell_info');
  const [sortConfig, setSortConfig] = useState<SortConfig>({ key: null, direction: 'ascending' });
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState<number | 'all'>(10);

  const cellInfoMap = useMemo(() => {
    const map: Record<number, { lat: number; lng: number }> = {};
    data.forEach(item => {
      map[item.id] = { lat: item.lat, lng: item.lng };
    });
    return map;
  }, [data]);

  const filteredTestData = useMemo(() => {
    if (activeSubTab === 'cell_info') return [];
    return testData.filter(test => test.type_ === activeSubTab);
  }, [testData, activeSubTab]);

  const sortedCellInfo = useMemo(() => {
    if (!sortConfig.key || activeSubTab !== 'cell_info') return data;

    return [...data].sort((a, b) => {
      if (a[sortConfig.key as keyof UEData] < b[sortConfig.key as keyof UEData]) {
        return sortConfig.direction === 'ascending' ? -1 : 1;
      }
      if (a[sortConfig.key as keyof UEData] > b[sortConfig.key as keyof UEData]) {
        return sortConfig.direction === 'ascending' ? 1 : -1;
      }
      return 0;
    });
  }, [data, sortConfig, activeSubTab]);

  const sortedTestData = useMemo(() => {
    if (!sortConfig.key || activeSubTab === 'cell_info') return filteredTestData;

    return [...filteredTestData].sort((a, b) => {
      const key = sortConfig.key!;

      if (key === 'location') {
        const aLocation = cellInfoMap[a.cell_info];
        const bLocation = cellInfoMap[b.cell_info];
        const aValue = aLocation ? `${aLocation.lat},${aLocation.lng}` : '';
        const bValue = bLocation ? `${bLocation.lat},${bLocation.lng}` : '';

        if (aValue < bValue) return sortConfig.direction === 'ascending' ? -1 : 1;
        if (aValue > bValue) return sortConfig.direction === 'ascending' ? 1 : -1;
        return 0;
      }

      const aValue = a[key as keyof TestData] || a.detail[key];
      const bValue = b[key as keyof TestData] || b.detail[key];

      if (aValue < bValue) return sortConfig.direction === 'ascending' ? -1 : 1;
      if (aValue > bValue) return sortConfig.direction === 'ascending' ? 1 : -1;
      return 0;
    });
  }, [filteredTestData, sortConfig, activeSubTab, cellInfoMap]);

  const currentItems = useMemo(() => {
    if (activeSubTab === 'cell_info') {
      if (itemsPerPage === 'all') return sortedCellInfo;
      const startIndex = (currentPage - 1) * itemsPerPage;
      return sortedCellInfo.slice(startIndex, startIndex + itemsPerPage);
    } else {
      if (itemsPerPage === 'all') return sortedTestData;
      const startIndex = (currentPage - 1) * itemsPerPage;
      return sortedTestData.slice(startIndex, startIndex + itemsPerPage);
    }
  }, [sortedCellInfo, sortedTestData, currentPage, itemsPerPage, activeSubTab]);

  const totalItems = useMemo(() => {
    return activeSubTab === 'cell_info' ? sortedCellInfo.length : sortedTestData.length;
  }, [sortedCellInfo, sortedTestData, activeSubTab]);

  const totalPages = itemsPerPage === 'all' ? 1 : Math.ceil(totalItems / itemsPerPage);

  const isUEData = (item: TableItem): item is UEData => {
    return (item as UEData).lat !== undefined;
  };

  const isTestData = (item: TableItem): item is TestData => {
    return (item as TestData).type_ !== undefined;
  };

  const handleSort = (key: string) => {
    let direction: 'ascending' | 'descending' = 'ascending';
    if (sortConfig.key === key && sortConfig.direction === 'ascending') {
      direction = 'descending';
    }
    setSortConfig({ key, direction });
    setCurrentPage(1);
  };

  const getSortIndicator = (key: string) => {
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

  const renderCellInfoTable = () => (
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
              onClick={() => handleSort(key)}
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
              {getSortIndicator(key)}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {currentItems.map((item) => {
          if (!isUEData(item)) return null;
          return (
            <tr key={item.id} className="border-b border-ocean-700 hover:bg-ocean-700">
              <td dir="ltr" className="py-2 px-2 text-center">{item.id}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.phone_number || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">
                {item.timestamp ? new Date(item.timestamp).toLocaleString('fa-IR') : '—'}
              </td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.lat.toFixed(6) || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.lng.toFixed(6) || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.gen || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.tech || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.plmn || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.cid || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.lac || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.rac || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.tac || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.freq_band || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.afrn || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.freq || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.rsrp || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.rsrq || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.ecno || '—'}</td>
              <td dir="ltr" className="py-2 px-2 text-center">{item.rxlev || '—'}</td>
            </tr>
          );
        })}
      </tbody>
    </table>
  );

  const renderTestDataTable = () => {
    const getColumns = () => {
      switch (activeSubTab) {
        case 'http_download':
        case 'http_upload':
          return [
            { key: 'id', label: 'ID' },
            { key: 'phone_number', label: 'شماره تلفن' },
            { key: 'timestamp', label: 'زمان' },
            { key: 'location', label: 'موقعیت' },
            { key: 'throughput', label: 'سرعت (مگابیت بر ثانیه)' },
          ];
        case 'dns':
          return [
            { key: 'id', label: 'ID' },
            { key: 'phone_number', label: 'شماره تلفن' },
            { key: 'timestamp', label: 'زمان' },
            { key: 'location', label: 'موقعیت' },
            { key: 'time', label: 'زمان پاسخ (میلی ثانیه)' },
          ];
        case 'web':
          return [
            { key: 'id', label: 'ID' },
            { key: 'phone_number', label: 'شماره تلفن' },
            { key: 'timestamp', label: 'زمان' },
            { key: 'location', label: 'موقعیت' },
            { key: 'response_time', label: 'زمان پاسخ (میلی ثانیه)' },
          ];
        case 'sms':
          return [
            { key: 'id', label: 'ID' },
            { key: 'phone_number', label: 'شماره تلفن' },
            { key: 'timestamp', label: 'زمان' },
            { key: 'location', label: 'موقعیت' },
            { key: 'send_time', label: 'زمان ارسال (میلی ثانیه)' },
          ];
        case 'ping':
          return [
            { key: 'id', label: 'ID' },
            { key: 'phone_number', label: 'شماره تلفن' },
            { key: 'timestamp', label: 'زمان' },
            { key: 'location', label: 'موقعیت' },
            { key: 'latency', label: 'تأخیر (میلی ثانیه)' },
          ];
        default:
          return [];
      }
    };

    const columns = getColumns();

    return (
      <table className="w-full text-sm">
        <thead className="sticky top-0 bg-ocean-900">
          <tr>
            {columns.map((column) => (
              <th
                key={column.key}
                className="py-3 px-2 text-center cursor-pointer hover:bg-ocean-700 whitespace-nowrap"
                onClick={() => handleSort(column.key)}
              >
                {column.label}
                {getSortIndicator(column.key)}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {currentItems.map((item) => {
            if (!isTestData(item)) return null;
            const location = cellInfoMap[item.cell_info];
            return (
              <tr key={item.id} className="border-b border-ocean-700 hover:bg-ocean-700">
                {columns.map((column) => {
                  let value = '—';

                  switch (column.key) {
                    case 'id':
                      value = item.id.toString();
                      break;
                    case 'phone_number':
                      value = item.phone_number;
                      break;
                    case 'timestamp':
                      value = new Date(item.timestamp).toLocaleString('fa-IR');
                      break;
                    case 'location':
                      value = location ? `${location.lat.toFixed(6)}, ${location.lng.toFixed(6)}` : '—';
                      break;
                    default:
                      value = item.detail[column.key] || '—';
                      break;
                  }

                  return (
                    <td key={column.key} dir="ltr" className="py-2 px-2 text-center">
                      {value}
                    </td>
                  );
                })}
              </tr>
            );
          })}
        </tbody>
      </table>
    );
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

      <div className="flex border-b border-ocean-700">
        <button
          className={`py-2 px-4 ${activeSubTab === 'cell_info' ? 'border-b-2 border-primary text-primary' : 'text-gray-300'}`}
          onClick={() => setActiveSubTab('cell_info')}
        >
          Cell Info
        </button>
        <button
          className={`py-2 px-4 ${activeSubTab === 'http_download' ? 'border-b-2 border-primary text-primary' : 'text-gray-300'}`}
          onClick={() => setActiveSubTab('http_download')}
        >
          HTTP Download
        </button>
        <button
          className={`py-2 px-4 ${activeSubTab === 'http_upload' ? 'border-b-2 border-primary text-primary' : 'text-gray-300'}`}
          onClick={() => setActiveSubTab('http_upload')}
        >
          HTTP Upload
        </button>
        <button
          className={`py-2 px-4 ${activeSubTab === 'dns' ? 'border-b-2 border-primary text-primary' : 'text-gray-300'}`}
          onClick={() => setActiveSubTab('dns')}
        >
          DNS
        </button>
        <button
          className={`py-2 px-4 ${activeSubTab === 'web' ? 'border-b-2 border-primary text-primary' : 'text-gray-300'}`}
          onClick={() => setActiveSubTab('web')}
        >
          Web
        </button>
        <button
          className={`py-2 px-4 ${activeSubTab === 'sms' ? 'border-b-2 border-primary text-primary' : 'text-gray-300'}`}
          onClick={() => setActiveSubTab('sms')}
        >
          SMS
        </button>
        <button
          className={`py-2 px-4 ${activeSubTab === 'ping' ? 'border-b-2 border-primary text-primary' : 'text-gray-300'}`}
          onClick={() => setActiveSubTab('ping')}
        >
          Ping
        </button>
      </div>

      <div className="flex-1 overflow-auto">
        {activeSubTab === 'cell_info' ? renderCellInfoTable() : renderTestDataTable()}
      </div>

      <div className="flex justify-between items-center p-4 border-t border-ocean-700">
        <div className="text-sm text-gray-300">
          نمایش {itemsPerPage === 'all'
            ? 1
            : ((currentPage - 1) * (itemsPerPage as number)) + 1
          } تا {itemsPerPage === 'all'
            ? totalItems
            : Math.min(currentPage * (itemsPerPage as number), totalItems)
          } از {totalItems} مورد
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
