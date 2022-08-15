import Pinia from "../lib/pinia.js";
import Axios from "../lib/axios.js";

const api = Axios.create({
  timeout: 5000,
});
api.interceptors.response.use((response) => response, (error) => {
  alert(error);
  throw error;
});

async function getUserKeyList() {
  const response = await api.get("/user/key/pair");
  return response.data.body;
}

async function getUserAutoTradingList() {
  const response = await api.get("/user/autotrading");
  return response.data.body;
}

async function getUserBackTestingList() {
  const response = await api.get("/user/backtesting");
  return response.data.body;
}

async function getTypeInfo() {
  const response = await api.get("/type");
  return response.data.body;
}

async function getStrategyMeta(strategyCode) {
  if (strategyCode) {
    const response = await api.get("/" + strategyCode + "/meta");
    return response.data.body.paramMetaList;
  }
  return [];
}

export const useDashboardStore = Pinia.defineStore('dashboard', {
  state: () => ({
    user: {
      keyPairList: [],
      autoTradingList: [],
      backTestingList: []
    },
    register: {
      keyPair: {},
      autoTrading: {},
      backTesting: {}
    },
    type: {}
  }),
  actions: {
    async refresh() {
      this.user.keyPairList = await getUserKeyList();
      this.user.autoTradingList = await getUserAutoTradingList();
      this.user.backTestingList = await getUserBackTestingList();
      this.type = await getTypeInfo();
      this.register.autoTrading.strategyCoreParamMetaList = await getStrategyMeta(
          this.register.autoTrading.strategyCode
      );
    }
  },
  getters: {}
})