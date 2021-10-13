/**
 * 防抖, 时间范围内只执行一次，反复执行会重置时间
 */
export default function debounce(fun, time = 200) {
    let timeout = 0;
    return function (...args) {
        if(timeout) clearTimeout(timeout);
        timeout = setTimeout(fun.apply(fun, args), time);
    }
}