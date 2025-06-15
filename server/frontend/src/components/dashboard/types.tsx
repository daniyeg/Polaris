export type UEData = {
  lat: number;
  lng: number;
  timestamp: number;
  gen: string;
  tech: string;
  plmn: number;
  lac: number;
  rac: number;
  tac: number;
  freq_band: string;
  afrn: number;
  freq: number;
  rsrp: number;
  rsrq: number;
  ecn0: number;
  rxlev: number;
};

export type DashboardTabProps = {
  data: UEData[];
  isLoading?: boolean;
};
