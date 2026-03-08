import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN = __ENV.TOKEN || '';
const PARTY_ID = Number(__ENV.PARTY_ID || 1);

const N = Number(__ENV.N || 10);
const START_INVITEE_ID = Number(__ENV.START_INVITEE_ID || 2);

const ITERATIONS = Number(__ENV.ITERATIONS || 20);

export const options = {
  scenarios: {
    concurrent_check: {
      executor: 'per-vu-iterations',
      vus: 30,
      iterations: 20,
      maxDuration: '10m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000', 'p(99)<1500'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

function buildInviteeIds(startId, n) {
  const ids = [];
  for (let i = 0; i < n; i++) {
    ids.push(startId + i);
  }
  return ids;
}

export default function () {
  const requestIndex = ((__VU - 1) * ITERATIONS) + __ITER;

  const partyId = PARTY_ID + requestIndex;

  const payload = JSON.stringify({
    invitee_ids: buildInviteeIds(START_INVITEE_ID, N),
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      ...(TOKEN ? { Authorization: `Bearer ${TOKEN}` } : {}),
    },
    tags: {
      api: 'party_invite',
      n: String(N),
      vu: String(__VU),
      iter: String(__ITER),
      partyId: String(partyId),
    },
  };

  const url = `${BASE_URL}/api/party/invite/${partyId}`;
  const res = http.post(url, payload, params);

  check(res, {
    'status is 201 or 200': (r) => r.status === 201 || r.status === 200,
  });

  if (res.status !== 200 && res.status !== 201) {
    console.log(
      `FAIL vu=${__VU} iter=${__ITER} partyId=${partyId} status=${res.status} body=${res.body}`
    );
  }
}
