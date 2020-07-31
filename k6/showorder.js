import http from 'k6/http';
import { sleep, check } from 'k6';

const rps = 10.0;
const think = 1.0/rps;

export default function() {
  const delay = think * Math.random();
  sleep(delay);
  let orderid=(10000000 * __ENV.RUN + 100000 * __VU + __ITER).toString();
  const res = http.get(`http://${__ENV.LB}:8080/showordernocache?orderid=${orderid}`);
  sleep(think - delay);

  const checkRes = check(res, {
    'status is 200': r => r.status === 200,
    'status is other': r => r.status !== 200,
  });

}