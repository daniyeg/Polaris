'use client'

import { PieChart, Pie, Cell, BarChart, Bar, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, AreaChart, Area } from 'recharts';
import { UEData } from '@/components/dashboard/types';
import { useState, useEffect } from 'react';
import { FaChartPie, FaChartLine, FaChartBar, FaChartArea } from 'react-icons/fa';
import { processPieChartData, processLineChartData, processBarChartData } from '@/utils/chartData';

const OCEAN_COLORS = [
  '#0ea5e9',
  '#14b8a6',
  '#8b5cf6',
  '#ec4899',
  '#f97316',
  '#0284c7',
  '#0d9488',
];

const generateColorShades = (baseColor: string, count: number): string[] => {
  const shades = [];
  for (let i = 0; i < count; i++) {
    const lightness = 40 + (i * (40 / count));
    shades.push(`color-mix(in srgb, ${baseColor} ${lightness}%, white)`);
  }
  return shades;
};

const getPieChartColors = (count: number): string[] => {
  if (count <= OCEAN_COLORS.length) {
    return OCEAN_COLORS.slice(0, count);
  }

  const colorsNeeded = count - OCEAN_COLORS.length;
  const shadesPerColor = Math.ceil(colorsNeeded / OCEAN_COLORS.length);

  let allColors = [...OCEAN_COLORS];

  OCEAN_COLORS.forEach(color => {
    const shades = generateColorShades(color, shadesPerColor);
    allColors = [...allColors, ...shades];
  });

  return allColors.slice(0, count);
};

const RADIAN = Math.PI / 180;

const renderCustomizedLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent, name }: any) => {
  const radius = innerRadius + (outerRadius - innerRadius) * 0.5;
  const x = cx + radius * Math.cos(-midAngle * RADIAN);
  const y = cy + radius * Math.sin(-midAngle * RADIAN);

  return (
    <text x={x} y={y} fill="white" textAnchor={x > cx ? 'start' : 'end'} dominantBaseline="central">
      {`${(percent * 100).toFixed(0)}%`}
    </text>
  );
};

const CHART_STYLES = {
  axis: {
    stroke: '#bae6fd',
    tick: { fill: '#bae6fd', fontSize: 12 },
  },
  grid: {
    stroke: '#0369a1',
    strokeDasharray: '3 3',
  },
  tooltip: {
    backgroundColor: '#075985',
    borderColor: '#0ea5e9',
    color: '#f0f9ff',
  },
  legend: {
    color: '#e0f2fe',
  }
};

const CustomYAxisTick = (props: any) => {
  const { x, y, payload } = props;
  return (
    <g transform={`translate(${x},${y})`}>
      <text
        x={-15}
        y={0}
        dy={4}
        textAnchor="end"
        fill="#bae6fd"
        fontSize={12}
      >
        {payload.value}
      </text>
    </g>
  );
};

const CustomXAxisTick = (props: any) => {
  const { x, y, payload } = props;
  return (
    <g transform={`translate(${x},${y})`}>
      <text
        x={0}
        y={0}
        dy={16}
        textAnchor="middle"
        fill="#bae6fd"
        fontSize={12}
      >
        {payload.value}
      </text>
    </g>
  );
};

export default function ChartsTab({ data }: { data: UEData[] }) {
  const [activeChartType, setActiveChartType] = useState<'pie' | 'line' | 'bar' | 'area'>('pie');
  const [chartData, setChartData] = useState<any>({
    techAdoption: [],
    freqBandUtilization: [],
    signalQuality: [],
    plmnShare: [],
    tacCoverage: [],
    cellCoverage: [],
    genDistribution: [],
    signalQualityLifetime: [],
    signalOverTime: [],
    freqBandPerformance: [],
    techPerformance: [],
    plmnPerformance: [],
    rsrpDistribution: [],
    rsrqDistribution: []
  });

  useEffect(() => {
    processChartData();
  }, [data, activeChartType]);

  const processChartData = () => {
    if (activeChartType === 'pie') {
      setChartData(processPieChartData(data));
    } else if (activeChartType === 'line') {
      setChartData(processLineChartData(data));
    } else if (activeChartType === 'bar') {
      setChartData(processBarChartData(data));
    } else {
      setChartData(processLineChartData(data));
    }
  };

  const renderPieCharts = () => {
    const techAdoptionColors = getPieChartColors((chartData.techAdoption || []).length);
    const freqBandColors = getPieChartColors((chartData.freqBandUtilization || []).length);
    const genDistributionColors = getPieChartColors((chartData.genDistribution || []).length);
    const plmnShareColors = getPieChartColors((chartData.plmnShare || []).length);
    const tacCoverageColors = getPieChartColors((chartData.tacCoverage || []).length);
    const signalQualityLifetimeColors = getPieChartColors((chartData.signalQualityLifetime || []).length);
    const signalQualityColors = getPieChartColors((chartData.signalQuality || []).length);

    return (
      <>
        <div className="bg-ocean-700 rounded-lg p-4 shadow-md">
          <h3 className="text-md font-semibold mb-4 text-center">توزیع فناوری شبکه (بر اساس زمان فعال)</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={chartData.techAdoption || []}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={renderCustomizedLabel}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
                nameKey="name"
              >
                {(chartData.techAdoption || []).map((_: any, index: number) => (
                  <Cell key={`cell-${index}`} fill={techAdoptionColors[index % techAdoptionColors.length]} />
                ))}
              </Pie>
              <Tooltip
                formatter={(value) => [`${Number(value).toFixed(2)}%`, 'درصد زمان']}
                contentStyle={CHART_STYLES.tooltip}
              />
              <Legend
                layout="horizontal"
                verticalAlign="bottom"
                align="center"
                wrapperStyle={CHART_STYLES.legend}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-ocean-700 rounded-lg p-4 shadow-md">
          <h3 className="text-md font-semibold mb-4 text-center">توزیع باند فرکانسی (بر اساس زمان فعال)</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={chartData.freqBandUtilization || []}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={renderCustomizedLabel}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
                nameKey="name"
              >
                {(chartData.freqBandUtilization || []).map((_: any, index: number) => (
                  <Cell key={`cell-${index}`} fill={freqBandColors[index % freqBandColors.length]} />
                ))}
              </Pie>
              <Tooltip
                formatter={(value) => [`${Number(value).toFixed(2)}%`, 'درصد زمان']}
                contentStyle={CHART_STYLES.tooltip}
              />
              <Legend
                layout="horizontal"
                verticalAlign="bottom"
                align="center"
                wrapperStyle={CHART_STYLES.legend}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-ocean-700 rounded-lg p-4 shadow-md">
          <h3 className="text-md font-semibold mb-4 text-center">توزیع نسل شبکه (بر اساس زمان فعال)</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={chartData.genDistribution || []}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={renderCustomizedLabel}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
                nameKey="name"
              >
                {(chartData.genDistribution || []).map((_: any, index: number) => (
                  <Cell key={`cell-${index}`} fill={genDistributionColors[index % genDistributionColors.length]} />
                ))}
              </Pie>
              <Tooltip
                formatter={(value) => [`${Number(value).toFixed(2)}%`, 'درصد زمان']}
                contentStyle={CHART_STYLES.tooltip}
              />
              <Legend
                layout="horizontal"
                verticalAlign="bottom"
                align="center"
                wrapperStyle={CHART_STYLES.legend}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-ocean-700 rounded-lg p-4 shadow-md">
          <h3 className="text-md font-semibold mb-4 text-center">سهم اپراتورها (PLMN) (بر اساس زمان فعال)</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={chartData.plmnShare || []}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={renderCustomizedLabel}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
                nameKey="name"
              >
                {(chartData.plmnShare || []).map((_: any, index: number) => (
                  <Cell key={`cell-${index}`} fill={plmnShareColors[index % plmnShareColors.length]} />
                ))}
              </Pie>
              <Tooltip
                formatter={(value) => [`${Number(value).toFixed(2)}%`, 'درصد زمان']}
                contentStyle={CHART_STYLES.tooltip}
              />
              <Legend
                layout="horizontal"
                verticalAlign="bottom"
                align="center"
                wrapperStyle={CHART_STYLES.legend}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-ocean-700 rounded-lg p-4 shadow-md">
          <h3 className="text-md font-semibold mb-4 text-center">پوشش منطقه‌ای (TAC) (بر اساس زمان فعال)</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={chartData.tacCoverage || []}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={renderCustomizedLabel}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
                nameKey="name"
              >
                {(chartData.tacCoverage || []).map((_: any, index: number) => (
                  <Cell key={`cell-${index}`} fill={tacCoverageColors[index % tacCoverageColors.length]} />
                ))}
              </Pie>
              <Tooltip
                formatter={(value) => [`${Number(value).toFixed(2)}%`, 'درصد زمان']}
                contentStyle={CHART_STYLES.tooltip}
              />
              <Legend
                layout="horizontal"
                verticalAlign="bottom"
                align="center"
                wrapperStyle={CHART_STYLES.legend}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-ocean-700 rounded-lg p-4 shadow-md">
          <h3 className="text-md font-semibold mb-4 text-center">توزیع کیفیت سیگنال (بر اساس زمان فعال)</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={chartData.signalQualityLifetime || []}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={renderCustomizedLabel}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
                nameKey="name"
              >
                {(chartData.signalQualityLifetime || []).map((_: any, index: number) => (
                  <Cell key={`cell-${index}`} fill={signalQualityLifetimeColors[index % signalQualityLifetimeColors.length]} />
                ))}
              </Pie>
              <Tooltip
                formatter={(value) => [`${Number(value).toFixed(2)}%`, 'درصد زمان']}
                contentStyle={CHART_STYLES.tooltip}
              />
              <Legend
                layout="horizontal"
                verticalAlign="bottom"
                align="center"
                wrapperStyle={CHART_STYLES.legend}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-ocean-700 rounded-lg p-4 shadow-md">
          <h3 className="text-md font-semibold mb-4 text-center">توزیع کیفیت سیگنال (بر اساس تعداد نمونه‌ها)</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={chartData.signalQuality || []}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={renderCustomizedLabel}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
                nameKey="name"
              >
                {(chartData.signalQuality || []).map((_: any, index: number) => (
                  <Cell key={`cell-${index}`} fill={signalQualityColors[index % signalQualityColors.length]} />
                ))}
              </Pie>
              <Tooltip
                formatter={(value) => [`${value} نمونه`, 'تعداد']}
                contentStyle={CHART_STYLES.tooltip}
              />
              <Legend
                layout="horizontal"
                verticalAlign="bottom"
                align="center"
                wrapperStyle={CHART_STYLES.legend}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </>
    );
  };

  const renderLineCharts = () => (
    <>
      <div className="bg-ocean-700 rounded-lg p-4 shadow-md col-span-2">
        <h3 className="text-md font-semibold mb-4 text-center">میانگین قدرت و کیفیت سیگنال بر اساس زمان</h3>
        <ResponsiveContainer width="100%" height={400}>
          <LineChart
            data={chartData.signalOverTime || []}
            margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke={CHART_STYLES.grid.stroke} />
            <XAxis
              dataKey="time"
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomXAxisTick />}
              interval={0}
              angle={-45}
              textAnchor="end"
              height={60}
            />
            <YAxis
              yAxisId="left"
              domain={[-130, -70]}
              label={{ value: 'RSRP (dBm)', angle: -90, position: 'insideLeft', fill: CHART_STYLES.axis.stroke }}
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomYAxisTick />}
              width={60}
            />
            <YAxis
              yAxisId="right"
              orientation="right"
              domain={[-20, 0]}
              label={{ value: 'RSRQ (dB)', angle: 90, position: 'insideRight', fill: CHART_STYLES.axis.stroke }}
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomYAxisTick />}
              width={60}
            />
            <Tooltip
              formatter={(value, name) => {
                if (name === 'avgRsrp') return [`${value} dBm`, 'میانگین RSRP'];
                if (name === 'avgRsrq') return [`${value} dB`, 'میانگین RSRQ'];
                return [value, name];
              }}
              contentStyle={CHART_STYLES.tooltip}
            />
            <Legend wrapperStyle={CHART_STYLES.legend} />
            <Line
              yAxisId="left"
              type="monotone"
              dataKey="avgRsrp"
              stroke="#0ea5e9"
              activeDot={{ r: 8 }}
              name="میانگین قدرت سیگنال (RSRP)"
            />
            <Line
              yAxisId="right"
              type="monotone"
              dataKey="avgRsrq"
              stroke="#14b8a6"
              name="میانگین کیفیت سیگنال (RSRQ)"
            />
          </LineChart>
        </ResponsiveContainer>
      </div>

      <div className="bg-ocean-700 rounded-lg p-4 shadow-md col-span-2">
        <h3 className="text-md font-semibold mb-4 text-center">میانگین قدرت سیگنال بر اساس باند فرکانسی</h3>
        <ResponsiveContainer width="100%" height={400}>
          <LineChart
            data={chartData.freqBandPerformance || []}
            margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke={CHART_STYLES.grid.stroke} />
            <XAxis
              dataKey="band"
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomXAxisTick />}
            />
            <YAxis
              domain={['dataMin - 5', 'dataMax + 5']}
              label={{ value: 'میانگین RSRP (dBm)', angle: -90, position: 'insideLeft', fill: CHART_STYLES.axis.stroke }}
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomYAxisTick />}
              width={60}
            />
            <Tooltip
              formatter={(value) => [`${value} dBm`, 'میانگین RSRP']}
              contentStyle={CHART_STYLES.tooltip}
            />
            <Legend wrapperStyle={CHART_STYLES.legend} />
            <Line
              type="monotone"
              dataKey="avgRsrp"
              stroke="#0ea5e9"
              activeDot={{ r: 8 }}
              name="میانگین قدرت سیگنال"
            />
          </LineChart>
        </ResponsiveContainer>
      </div>

      <div className="bg-ocean-700 rounded-lg p-4 shadow-md col-span-2">
        <h3 className="text-md font-semibold mb-4 text-center">میانگین قدرت سیگنال بر اساس فناوری</h3>
        <ResponsiveContainer width="100%" height={400}>
          <LineChart
            data={chartData.techPerformance || []}
            margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke={CHART_STYLES.grid.stroke} />
            <XAxis
              dataKey="tech"
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomXAxisTick />}
            />
            <YAxis
              domain={['dataMin - 5', 'dataMax + 5']}
              label={{ value: 'میانگین RSRP (dBm)', angle: -90, position: 'insideLeft', fill: CHART_STYLES.axis.stroke }}
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomYAxisTick />}
              width={60}
            />
            <Tooltip
              formatter={(value) => [`${value} dBm`, 'میانگین RSRP']}
              contentStyle={CHART_STYLES.tooltip}
            />
            <Legend wrapperStyle={CHART_STYLES.legend} />
            <Line
              type="monotone"
              dataKey="avgRsrp"
              stroke="#0ea5e9"
              activeDot={{ r: 8 }}
              name="میانگین قدرت سیگنال"
            />
          </LineChart>
        </ResponsiveContainer>
      </div>

      <div className="bg-ocean-700 rounded-lg p-4 shadow-md col-span-2">
        <h3 className="text-md font-semibold mb-4 text-center">میانگین قدرت سیگنال بر اساس اپراتور</h3>
        <ResponsiveContainer width="100%" height={400}>
          <LineChart
            data={chartData.plmnPerformance || []}
            margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke={CHART_STYLES.grid.stroke} />
            <XAxis
              dataKey="plmn"
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomXAxisTick />}
            />
            <YAxis
              domain={['dataMin - 5', 'dataMax + 5']}
              label={{ value: 'میانگین RSRP (dBm)', angle: -90, position: 'insideLeft', fill: CHART_STYLES.axis.stroke }}
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomYAxisTick />}
              width={60}
            />
            <Tooltip
              formatter={(value) => [`${value} dBm`, 'میانگین RSRP']}
              contentStyle={CHART_STYLES.tooltip}
            />
            <Legend wrapperStyle={CHART_STYLES.legend} />
            <Line
              type="monotone"
              dataKey="avgRsrp"
              stroke="#0ea5e9"
              activeDot={{ r: 8 }}
              name="میانگین قدرت سیگنال"
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </>
  );

  const renderBarCharts = () => (
    <>
      <div className="bg-ocean-700 rounded-lg p-4 shadow-md col-span-2">
        <h3 className="text-md font-semibold mb-4 text-center">توزیع قدرت سیگنال (RSRP)</h3>
        <ResponsiveContainer width="100%" height={400}>
          <BarChart
            data={chartData.rsrpDistribution || []}
            margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke={CHART_STYLES.grid.stroke} />
            <XAxis
              dataKey="range"
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomXAxisTick />}
            />
            <YAxis
              label={{ value: 'تعداد نمونه‌ها', angle: -90, position: 'insideLeft', fill: CHART_STYLES.axis.stroke }}
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomYAxisTick />}
              width={60}
            />
            <Tooltip
              formatter={(value) => [`${value} نمونه`, 'تعداد']}
              contentStyle={CHART_STYLES.tooltip}
            />
            <Legend wrapperStyle={CHART_STYLES.legend} />
            <Bar dataKey="count" name="تعداد نمونه‌ها" fill="#0ea5e9" />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="bg-ocean-700 rounded-lg p-4 shadow-md col-span-2">
        <h3 className="text-md font-semibold mb-4 text-center">توزیع کیفیت سیگنال (RSRQ)</h3>
        <ResponsiveContainer width="100%" height={400}>
          <BarChart
            data={chartData.rsrqDistribution || []}
            margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke={CHART_STYLES.grid.stroke} />
            <XAxis
              dataKey="range"
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomXAxisTick />}
            />
            <YAxis
              label={{ value: 'تعداد نمونه‌ها', angle: -90, position: 'insideLeft', fill: CHART_STYLES.axis.stroke }}
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomYAxisTick />}
              width={60}
            />
            <Tooltip
              formatter={(value) => [`${value} نمونه`, 'تعداد']}
              contentStyle={CHART_STYLES.tooltip}
            />
            <Legend wrapperStyle={CHART_STYLES.legend} />
            <Bar dataKey="count" name="تعداد نمونه‌ها" fill="#14b8a6" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </>
  );

  const renderAreaCharts = () => (
    <>
      <div className="bg-ocean-700 rounded-lg p-4 shadow-md col-span-2">
        <h3 className="text-md font-semibold mb-4 text-center">روند تغییرات قدرت سیگنال در طول زمان</h3>
        <ResponsiveContainer width="100%" height={400}>
          <AreaChart
            data={chartData.signalOverTime || []}
            margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke={CHART_STYLES.grid.stroke} />
            <XAxis
              dataKey="time"
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomXAxisTick />}
              interval={0}
              angle={-45}
              textAnchor="end"
              height={60}
            />
            <YAxis
              domain={[-130, -70]}
              label={{ value: 'RSRP (dBm)', angle: -90, position: 'insideLeft', fill: CHART_STYLES.axis.stroke }}
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomYAxisTick />}
              width={60}
            />
            <Tooltip
              formatter={(value) => [`${value} dBm`, 'میانگین RSRP']}
              contentStyle={CHART_STYLES.tooltip}
            />
            <Legend wrapperStyle={CHART_STYLES.legend} />
            <Area
              type="monotone"
              dataKey="avgRsrp"
              stroke="#0ea5e9"
              fill="#0ea5e9"
              fillOpacity={0.3}
              name="میانگین قدرت سیگنال"
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      <div className="bg-ocean-700 rounded-lg p-4 shadow-md col-span-2">
        <h3 className="text-md font-semibold mb-4 text-center">روند تغییرات کیفیت سیگنال در طول時間</h3>
        <ResponsiveContainer width="100%" height={400}>
          <AreaChart
            data={chartData.signalOverTime || []}
            margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke={CHART_STYLES.grid.stroke} />
            <XAxis
              dataKey="time"
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomXAxisTick />}
              interval={0}
              angle={-45}
              textAnchor="end"
              height={60}
            />
            <YAxis
              domain={[-20, 0]}
              label={{ value: 'RSRQ (dB)', angle: -90, position: 'insideLeft', fill: CHART_STYLES.axis.stroke }}
              stroke={CHART_STYLES.axis.stroke}
              tick={<CustomYAxisTick />}
              width={60}
            />
            <Tooltip
              formatter={(value) => [`${value} dB`, 'میانگین RSRQ']}
              contentStyle={CHART_STYLES.tooltip}
            />
            <Legend wrapperStyle={CHART_STYLES.legend} />
            <Area
              type="monotone"
              dataKey="avgRsrq"
              stroke="#14b8a6"
              fill="#14b8a6"
              fillOpacity={0.3}
              name="میانگین کیفیت سیگنال"
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </>
  );

  return (
    <div className="h-full flex flex-col">
      <div className="flex border-b border-ocean-700 mb-4">
        <button
          className={`py-2 px-4 font-medium flex items-center ${activeChartType === 'pie' ? 'text-primary border-b-2 border-primary' : 'text-ocean-300 hover:text-ocean-200'}`}
          onClick={() => setActiveChartType('pie')}
        >
          <FaChartPie className="ml-2" /> نمودارهای دایره‌ای
        </button>
        <button
          className={`py-2 px-4 font-medium flex items-center ${activeChartType === 'line' ? 'text-primary border-b-2 border-primary' : 'text-ocean-300 hover:text-ocean-200'}`}
          onClick={() => setActiveChartType('line')}
        >
          <FaChartLine className="ml-2" /> نمودارهای خطی
        </button>
        <button
          className={`py-2 px-4 font-medium flex items-center ${activeChartType === 'bar' ? 'text-primary border-b-2 border-primary' : 'text-ocean-300 hover:text-ocean-200'}`}
          onClick={() => setActiveChartType('bar')}
        >
          <FaChartBar className="ml-2" /> نمودارهای میله‌ای
        </button>
        <button
          className={`py-2 px-4 font-medium flex items-center ${activeChartType === 'area' ? 'text-primary border-b-2 border-primary' : 'text-ocean-300 hover:text-ocean-200'}`}
          onClick={() => setActiveChartType('area')}
        >
          <FaChartArea className="ml-2" /> نمودارهای منطقه‌ای
        </button>
      </div>

      <div className="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4 p-2 overflow-auto">
        {activeChartType === 'pie' && renderPieCharts()}
        {activeChartType === 'line' && renderLineCharts()}
        {activeChartType === 'bar' && renderBarCharts()}
        {activeChartType === 'area' && renderAreaCharts()}
      </div>
    </div>
  );
}
