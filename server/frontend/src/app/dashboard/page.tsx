'use client'

import dynamic from 'next/dynamic';
import { useState, useEffect } from 'react';
import { FaChartPie } from 'react-icons/fa';
import { FaMapMarkedAlt, FaTable, FaFilter, FaSignOutAlt, FaClock } from 'react-icons/fa';
import { generateMockData } from '@/components/dashboard/GenerateMockData';
import { UEData } from '@/components/dashboard/types';
import { useRouter } from 'next/navigation';

const MapTab = dynamic(() => import('@/components/dashboard/Tabs/MapTab'));
const TableTab = dynamic(() => import('@/components/dashboard/Tabs/TableTab'));
const ChartsTab = dynamic(() => import('@/components/dashboard/Tabs/ChartsTab'));

export default function Dashboard() {
  const [activeTab, setActiveTab] = useState<string>('map');
  const [timeRange, setTimeRange] = useState<string>('all');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [data, setData] = useState<UEData[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [showTimeDropdown, setShowTimeDropdown] = useState<boolean>(false);
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      router.push('/');
    }
  }, [router]);

  const handleLogout = async () => {
    const token = localStorage.getItem('token');

    try {
      const response = await fetch('https://polaris-server-30ha.onrender.com/api/logout', {
        method: 'POST',
        headers: {
          'Authorization': `Token ${token}`,
        }
      });

      if (response.ok) {
        console.log('Token invalidated successfully');
      } else {
        console.warn('Logout API call failed.');
      }
    } catch (error) {
      console.error('Error during logout API call:', error);
    } finally {
      localStorage.removeItem('token');
      router.push('/');
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      const token = localStorage.getItem('token');
      setIsLoading(true);
      setData([]);
      setError(null);

      try {
        let url = 'https://polaris-server-30ha.onrender.com/api/get_cell_infos/';
        if (timeRange !== 'all') {
          url += `?range=${timeRange}`;
        }

        const response = await fetch(url, {
          method: 'GET',
          headers: {
            'Authorization': `Token ${token}`
          }
        });

        if (response.status === 403) {
          localStorage.removeItem('token');
          router.push('/login');
          return;
        }

        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(JSON.stringify(errorData) || 'خطا در دریافت اطلاعات');
        }

        const data = await response.json();
        setData(data);
      } catch (error) {
        console.error('خطای دریافت داده:', error);
        setError('دریافت داده‌ها با مشکل مواجه شد. لطفا دوباره تلاش کنید.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [timeRange, router]);

  const timeRangeOptions = [
    { value: '1h', label: '۱ ساعت اخیر' },
    { value: '24h', label: '۲۴ ساعت اخیر' },
    { value: '7d', label: '۷ روز اخیر' },
    { value: '2w', label: '۲ هفته اخیر'},
    { value: '30d', label: '۳۰ روز اخیر' },
    { value: 'all', label: 'همه' }
  ];

  const getTimeRangeLabel = (value: string) => {
    const option = timeRangeOptions.find(opt => opt.value === value);
    return option ? option.label : 'بازه زمانی';
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-background-from to-background-to text-foreground flex flex-col">
      <header className="bg-ocean-800 p-4 shadow-md relative z-[900]">
        <div className="container mx-auto flex justify-between items-center">
          <div className="flex items-center gap-4">
            <h1 className="text-2xl font-bold">سامانه Polaris</h1>

            <div className="relative">
              <button
                className="flex items-center text-white hover:text-gray-200 bg-ocean-700 py-2 px-3 rounded-md"
                onClick={() => setShowTimeDropdown(!showTimeDropdown)}
              >
                <FaClock className="ml-2" />
                <span>{getTimeRangeLabel(timeRange)}</span>
              </button>

              {showTimeDropdown && (
                <div className="absolute right-0 mt-2 w-48 bg-ocean-700 rounded-md shadow-lg z-[1000]">
                  <div className="py-1">
                    {timeRangeOptions.map(option => (
                      <button
                        key={option.value}
                        className={`block w-full text-right px-4 py-2 text-sm hover:bg-ocean-600 ${timeRange === option.value ? 'text-primary' : 'text-white'}`}
                        onClick={() => {
                          setTimeRange(option.value);
                          setShowTimeDropdown(false);
                        }}
                      >
                        {option.label}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>

          <div>
            <button
              onClick={handleLogout}
              className="flex items-center text-secondary hover:text-secondary-hover"
            >
              <FaSignOutAlt className="ml-1" /> خروج
            </button>
          </div>
        </div>
      </header>

      <div className="container mx-auto p-4 flex-1 flex flex-col relative z-0">
        <div className="flex border-b border-ocean-700 mb-4">
          <button
            className={`flex-1 py-4 px-2 font-medium flex items-center justify-center ${activeTab === 'map' ? 'text-primary border-b-2 border-primary' : 'text-ocean-300 hover:text-ocean-200'}`}
            onClick={() => setActiveTab('map')}
          >
            <FaMapMarkedAlt className="ml-2" /> نقشه
          </button>
          <button
            className={`flex-1 py-4 px-2 font-medium flex items-center justify-center ${activeTab === 'table' ? 'text-primary border-b-2 border-primary' : 'text-ocean-300 hover:text-ocean-200'}`}
            onClick={() => setActiveTab('table')}
          >
            <FaTable className="ml-2" /> جدول
          </button>
          <button
            className={`flex-1 py-4 px-2 font-medium flex items-center justify-center ${activeTab === 'charts' ? 'text-primary border-b-2 border-primary' : 'text-ocean-300 hover:text-ocean-200'}`}
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
          ) : error ? (
            <div className="flex flex-col justify-center items-center h-full p-4 text-center">
              <div className="text-red-400 text-xl mb-2">خطا در دریافت داده‌ها</div>
              <div className="text-gray-300 mb-4">{error}</div>
              <button
                onClick={() => window.location.reload()}
                className="bg-primary hover:bg-primary-hover text-white px-4 py-2 rounded"
              >
                تلاش مجدد
              </button>
            </div>
          ) : (
            <div className="h-full p-1">
              {activeTab === 'map' && <MapTab data={data} />}
              {activeTab === 'table' && <TableTab data={data} />}
              {activeTab === 'charts' && <ChartsTab data={data} />}
            </div>
          )}
        </div>
      </div>

      {showTimeDropdown && (
        <div
          className="fixed inset-0 z-0"
          onClick={() => setShowTimeDropdown(false)}
        />
      )}
    </div>
  );
}
