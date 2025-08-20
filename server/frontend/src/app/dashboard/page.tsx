'use client'

import Link from 'next/link';
import dynamic from 'next/dynamic';
import { useState, useEffect } from 'react';
import { FaChartPie } from 'react-icons/fa';
import { FaSignal, FaMapMarkedAlt, FaHistory, FaFilter, FaSignOutAlt } from 'react-icons/fa';
import { generateMockData } from '@/components/dashboard/GenerateMockData';
import { UEData } from '@/components/dashboard/types';

type Filters = {
  gen: string;
  tech: string;
  freqBand: string;
};

const MapTab = dynamic(() => import('@/components/dashboard/Tabs/MapTab'));
const SignalsTab = dynamic(() => import('@/components/dashboard/Tabs/SignalsTab'));
const HistoryTab = dynamic(() => import('@/components/dashboard/Tabs/HistoryTab'));
const ChartsTab = dynamic(() => import('@/components/dashboard/Tabs/ChartsTab'));

export default function Dashboard() {
  const [activeTab, setActiveTab] = useState<string>('map');
  const [timeRange, setTimeRange] = useState<string>('1h');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [data, setData] = useState<UEData[]>([]);
  const [filters, setFilters] = useState<Filters>({
    gen: '',
    tech: '',
    freqBand: ''
  });

  useEffect(() => {
    const fetchData = async () => {
      setIsLoading(true);
      setTimeout(() => {
        setData(generateMockData(100));
        setIsLoading(false);
      }, 1000);
    };
    fetchData();
  }, [timeRange, filters]);

  const handleFilterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-background-from to-background-to text-foreground flex flex-col">
      <header className="bg-ocean-800 p-4 shadow-md">
        <div className="container mx-auto flex justify-between items-center">
          <h1 className="text-2xl font-bold">سامانه Polaris</h1>
          <Link href="/" className="flex items-center text-secondary hover:text-secondary-hover">
            <FaSignOutAlt className="ml-1" /> خروج
          </Link>
        </div>
      </header>

      <div className="container mx-auto p-4 flex-1 flex flex-col">
        <div className="bg-ocean-800 rounded-lg p-4 mb-6 shadow-md">
          <h2 className="text-lg font-semibold mb-3 flex items-center">
            <FaFilter className="ml-2" /> فیلترها
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm mb-1">بازه زمانی</label>
              <select
                className="w-full bg-ocean-700 border border-ocean-600 rounded p-2 text-sm"
                value={timeRange}
                onChange={(e) => setTimeRange(e.target.value)}
              >
                <option value="15m">15 دقیقه اخیر</option>
                <option value="1h">1 ساعت اخیر</option>
                <option value="24h">24 ساعت اخیر</option>
                <option value="7d">7 روز اخیر</option>
              </select>
            </div>
            <div>
              <label className="block text-sm mb-1">نسل شبکه</label>
              <select
                className="w-full bg-ocean-700 border border-ocean-600 rounded p-2 text-sm"
                name="gen"
                value={filters.gen}
                onChange={handleFilterChange}
              >
                <option value="">همه</option>
                <option value="2G">2G</option>
                <option value="3G">3G</option>
                <option value="4G">4G</option>
                <option value="5G">5G</option>
              </select>
            </div>
            <div>
              <label className="block text-sm mb-1">باند فرکانسی</label>
              <select
                className="w-full bg-ocean-700 border border-ocean-600 rounded p-2 text-sm"
                name="freqBand"
                value={filters.freqBand}
                onChange={handleFilterChange}
              >
                <option value="">همه</option>
                <option value="B1">B1 (2100 MHz)</option>
                <option value="B3">B3 (1800 MHz)</option>
                <option value="B7">B7 (2600 MHz)</option>
                <option value="B8">B8 (900 MHz)</option>
                <option value="B20">B20 (800 MHz)</option>
                <option value="B38">B38 (2600 MHz TDD)</option>
              </select>
            </div>
          </div>
        </div>

        <div className="flex border-b border-ocean-700 mb-4">
          <button
            className={`py-3 px-6 font-medium flex items-center ${activeTab === 'map' ? 'text-primary border-b-2 border-primary' : 'text-ocean-300 hover:text-ocean-200'}`}
            onClick={() => setActiveTab('map')}
          >
            <FaMapMarkedAlt className="ml-2" /> نقشه
          </button>
          <button
            className={`py-3 px-6 font-medium flex items-center ${activeTab === 'signals' ? 'text-primary border-b-2 border-primary' : 'text-ocean-300 hover:text-ocean-200'}`}
            onClick={() => setActiveTab('signals')}
          >
            <FaSignal className="ml-2" /> سیگنال‌ها
          </button>
          <button
            className={`py-3 px-6 font-medium flex items-center ${activeTab === 'history' ? 'text-primary border-b-2 border-primary' : 'text-ocean-300 hover:text-ocean-200'}`}
            onClick={() => setActiveTab('history')}
          >
            <FaHistory className="ml-2" /> تاریخچه
          </button>
          <button
            className={`py-3 px-6 font-medium flex items-center ${activeTab === 'charts' ? 'text-primary border-b-2 border-primary' : 'text-ocean-300 hover:text-ocean-200'}`}
            onClick={() => setActiveTab('charts')}
          >
            <FaChartPie className="ml-2" /> نمودارها
          </button>
        </div>

        <div className="flex-1 bg-ocean-800 rounded-lg shadow-md overflow-hidden">
          {isLoading ? (
            <div className="flex justify-center items-center h-full">
              <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary"></div>
            </div>
          ) : (
            <div className="h-full p-1">
              {activeTab === 'map' && <MapTab data={data} />}
              {activeTab === 'signals' && <SignalsTab data={data} />}
              {activeTab === 'history' && <HistoryTab data={data} />}
              {activeTab === 'charts' && <ChartsTab data={data} />}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
