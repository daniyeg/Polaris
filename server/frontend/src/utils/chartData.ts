import { UEData } from '@/components/dashboard/types';

const categorizeRSRQ = (rsrq: number) => {
  if (rsrq >= -7) return 'عالی';
  if (rsrq >= -10) return 'خوب';
  if (rsrq >= -15) return 'متوسط';
  return 'ضعیف';
};

const parseTimestamp = (timestamp: string) => new Date(timestamp).getTime();

const calculateLifetimeByCategory = (data: UEData[], categoryKey: keyof UEData) => {
  const sortedData = [...data].sort((a, b) => parseTimestamp(a.timestamp) - parseTimestamp(b.timestamp));

  const categoryGroups: Record<string, UEData[]> = {};

  sortedData.forEach(item => {
    const categoryValue = String(item[categoryKey]);
    if (!categoryGroups[categoryValue]) {
      categoryGroups[categoryValue] = [];
    }
    categoryGroups[categoryValue].push(item);
  });

  const lifetimeByCategory: Record<string, number> = {};

  Object.entries(categoryGroups).forEach(([category, items]) => {
    items.sort((a, b) => parseTimestamp(a.timestamp) - parseTimestamp(b.timestamp));

    let totalLifetime = 0;

    for (let i = 0; i < items.length; i++) {
      const currentItem = items[i];
      const currentIndex = sortedData.findIndex(item =>
        item.timestamp === currentItem.timestamp &&
        String(item[categoryKey]) === category
      );

      if (currentIndex < sortedData.length - 1) {
        const nextItem = sortedData[currentIndex + 1];
        if (String(nextItem[categoryKey]) !== category) {
          totalLifetime += parseTimestamp(nextItem.timestamp) - parseTimestamp(currentItem.timestamp);
        }
      }
    }

    lifetimeByCategory[category] = totalLifetime;
  });

  return lifetimeByCategory;
};

export const processPieChartData = (data: UEData[]) => {
  if (data.length === 0) {
    return {
      techAdoption: [],
      freqBandUtilization: [],
      signalQuality: [],
      plmnShare: [],
      tacCoverage: [],
      cellCoverage: [],
      genDistribution: [],
      signalQualityLifetime: []
    };
  }

  const techLifetime = calculateLifetimeByCategory(data, 'tech');
  const freqBandLifetime = calculateLifetimeByCategory(data, 'freq_band');
  const plmnLifetime = calculateLifetimeByCategory(data, 'plmn');
  const tacLifetime = calculateLifetimeByCategory(data, 'tac');
  const genLifetime = calculateLifetimeByCategory(data, 'gen');

  const signalQualityData = data.map(item => ({
    ...item,
    signalQuality: categorizeRSRQ(item.rsrq)
  }));

  const signalQualityLifetime = calculateLifetimeByCategory(signalQualityData as UEData[], 'signalQuality' as keyof UEData);

  const signalQualityCounts: Record<string, number> = {};
  data.forEach(item => {
    const quality = categorizeRSRQ(item.rsrq);
    signalQualityCounts[quality] = (signalQualityCounts[quality] || 0) + 1;
  });

  const totalLifetime = Object.values(techLifetime).reduce((sum, time) => sum + time, 0);

  const toPercentageData = (lifetimeData: Record<string, number>) => {
    return Object.entries(lifetimeData).map(([name, value]) => ({
      name,
      value: totalLifetime > 0 ? (value / totalLifetime) * 100 : 0
    }));
  };

  const toCountData = (counts: Record<string, number>) => {
    return Object.entries(counts).map(([name, value]) => ({
      name,
      value
    }));
  };

  return {
    techAdoption: toPercentageData(techLifetime),
    freqBandUtilization: toPercentageData(freqBandLifetime),
    plmnShare: toPercentageData(plmnLifetime),
    tacCoverage: toPercentageData(tacLifetime),
    genDistribution: toPercentageData(genLifetime),
    signalQuality: toCountData(signalQualityCounts),
    signalQualityLifetime: toPercentageData(signalQualityLifetime)
  };
};

export const processLineChartData = (data: UEData[]) => {
  const timeSeries: Record<number, { rsrpSum: number; rsrqSum: number; count: number }> = {};
  const freqBandPerformance: Record<string, { rsrpSum: number; rsrqSum: number; count: number }> = {};
  const techPerformance: Record<string, { rsrpSum: number; rsrqSum: number; count: number }> = {};
  const plmnPerformance: Record<string, { rsrpSum: number; rsrqSum: number; count: number }> = {};

  data.forEach(item => {
    const timestampMs = parseTimestamp(item.timestamp);
    const timeBin = Math.floor(timestampMs / (5 * 60 * 1000)) * (5 * 60 * 1000);

    if (!timeSeries[timeBin]) {
      timeSeries[timeBin] = { rsrpSum: 0, rsrqSum: 0, count: 0 };
    }
    timeSeries[timeBin].rsrpSum += item.rsrp;
    timeSeries[timeBin].rsrqSum += item.rsrq;
    timeSeries[timeBin].count++;

    if (!freqBandPerformance[item.freq_band]) {
      freqBandPerformance[item.freq_band] = { rsrpSum: 0, rsrqSum: 0, count: 0 };
    }
    freqBandPerformance[item.freq_band].rsrpSum += item.rsrp;
    freqBandPerformance[item.freq_band].rsrqSum += item.rsrq;
    freqBandPerformance[item.freq_band].count++;

    if (!techPerformance[item.tech]) {
      techPerformance[item.tech] = { rsrpSum: 0, rsrqSum: 0, count: 0 };
    }
    techPerformance[item.tech].rsrpSum += item.rsrp;
    techPerformance[item.tech].rsrqSum += item.rsrq;
    techPerformance[item.tech].count++;

    const plmnKey = item.plmn.toString();
    if (!plmnPerformance[plmnKey]) {
      plmnPerformance[plmnKey] = { rsrpSum: 0, rsrqSum: 0, count: 0 };
    }
    plmnPerformance[plmnKey].rsrpSum += item.rsrp;
    plmnPerformance[plmnKey].rsrqSum += item.rsrq;
    plmnPerformance[plmnKey].count++;
  });

  const timeChartData = Object.entries(timeSeries)
    .map(([timestamp, { rsrpSum, rsrqSum, count }]) => ({
      time: new Date(Number(timestamp)).toLocaleTimeString('fa-IR', { hour: '2-digit', minute: '2-digit' }),
      avgRsrp: Math.round(rsrpSum / count),
      avgRsrq: Math.round(rsrqSum / count)
    }))
    .sort((a, b) => a.time.localeCompare(b.time));

  const freqBandData = Object.entries(freqBandPerformance)
    .map(([band, { rsrpSum, rsrqSum, count }]) => ({
      band,
      avgRsrp: Math.round(rsrpSum / count),
      avgRsrq: Math.round(rsrqSum / count)
    }))
    .sort((a, b) => b.avgRsrp - a.avgRsrp);

  const techData = Object.entries(techPerformance)
    .map(([tech, { rsrpSum, rsrqSum, count }]) => ({
      tech,
      avgRsrp: Math.round(rsrpSum / count),
      avgRsrq: Math.round(rsrqSum / count)
    }))
    .sort((a, b) => b.avgRsrp - a.avgRsrp);

  const plmnData = Object.entries(plmnPerformance)
    .map(([plmn, { rsrpSum, rsrqSum, count }]) => ({
      plmn: `PLMN ${plmn}`,
      avgRsrp: Math.round(rsrpSum / count),
      avgRsrq: Math.round(rsrqSum / count)
    }))
    .sort((a, b) => b.avgRsrp - a.avgRsrp);

  return {
    signalOverTime: timeChartData,
    freqBandPerformance: freqBandData,
    techPerformance: techData,
    plmnPerformance: plmnData
  };
};

export const processBarChartData = (data: UEData[]) => {
  const rsrpDistribution: Record<number, number> = {};
  const rsrqDistribution: Record<number, number> = {};

  data.forEach(item => {
    const rsrpBin = Math.floor(item.rsrp / 5) * 5;
    rsrpDistribution[rsrpBin] = (rsrpDistribution[rsrpBin] || 0) + 1;

    const rsrqBin = Math.floor(item.rsrq);
    rsrqDistribution[rsrqBin] = (rsrqDistribution[rsrqBin] || 0) + 1;
  });

  const toChartData = (distribution: Record<number, number>, labelPrefix: string) => {
    return Object.entries(distribution)
      .map(([bin, count]) => ({
        range: `${labelPrefix} ${bin} to ${Number(bin) + (labelPrefix === 'RSRP' ? 5 : 1)}`,
        count
      }))
      .sort((a, b) => a.range.localeCompare(b.range));
  };

  return {
    rsrpDistribution: toChartData(rsrpDistribution, 'RSRP'),
    rsrqDistribution: toChartData(rsrqDistribution, 'RSRQ')
  };
};
