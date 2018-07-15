#!/bin/bash


function print_help () {
    echo "This script allows to check if drifts are detected correctly"
    echo -e "-d DIRECTORY\n\tDirectory with *.txt files containing instances"
    echo -e "-s SAVE\n\tSaves results to a provided filename (preferably algorithm name)"
    echo -e "-t TOLERATION\n\tTakes a numberic value for toleration"
    echo -e "-i INSTANCES\n\tTakes value for number of instances in one file"
    echo -e "-h\n\tPrints this help message"
}

function print_red () {
    echo -e "\033[0;31m${*}\033[0m" >&2
}

function print_green () {
    echo -e "\033[0;32m${*}\033[0m" >&2
}

function print_yellow () {
    echo -e "\033[0;33m${*}\033[0m" >&2
}

function print_blue () {
    echo -e "\033[1;34m${*}\033[0m" >&2
}

function save_to_output () {
    local -r FILE=${1}
    local -r CORR=${2}
    local -r MISS=${3}
    local -r FAIL=${4}
    local -r AVG=${5}
    if [[ ! -z "${OUTPUT_DIR:-}" ]]; then
        echo "$(basename ${FILE%%.*}),${CORR},${MISS},${FAIL},${AVG}" >> ${OUTPUT_DIR}
    fi
}


while getopts ":hd:t:i:s:" OPT; do
  case ${OPT} in
    h)
        print_help
        exit 0
        ;;
    d)
        DIR=${OPTARG}
        ;;
    s)
        OUTPUT_DIR=${OPTARG}
        ;;
    t)
        TOLERATION=${OPTARG}
        ;;
    i)
        INSTANCES=${OPTARG}
        ;;
    \?)
        print_red "ERROR: Invalid option: -${OPTARG}"
        exit 1
        ;;
  esac
done



## DEFAUTLS AND WARNINGS ##

if [[ -z "${OUTPUT_DIR:-}" ]]; then
    print_red "You need to provide algorithm name for output directory!"
    exit 1
elif [[ ! -z "${OUTPUT_DIR:-}" ]]; then
    echo "Output will be saved to ${OUTPUT_DIR}.csv"
    OUTPUT_DIR+=".txt"
    echo "file,correct,missed,failed,average" > ${OUTPUT_DIR}
fi

if [[ ! -z "${DIR:-}" && ! -d "${DIR}" ]]; then
    print_red "Provided value is not a directory!"
    exit 1
elif [[ -z "${DIR:-}" || ! -d "${DIR}" ]]; then
    DIR="results"
    echo "Using default results directory: ${DIR}"
fi

NUMBER_REGEX="^[0-9]+$"

if [[ -z "${TOLERATION:-}" || ! "${TOLERATION}" =~ ${NUMBER_REGEX} ]]; then
    if [[ ! -z "${TOLERATION:-}" && ! "${TOLERATION}" =~ ${NUMBER_REGEX} ]]; then
        print_red "Provided toleration is not a number!"
    fi
    TOLERATION=5000
    echo "Using default toleration: ${TOLERATION}"
fi

if [[ -z "${INSTANCES:-}" || ! "${INSTANCES}" =~ ${NUMBER_REGEX} ]]; then
    if [[ ! -z "${INSTANCES:-}" && ! "${INSTANCES}" =~ ${NUMBER_REGEX} ]]; then
        print_red "Provided number of instances is not a number!"
    fi
    INSTANCES=100000
    echo "Using default number of instances: ${INSTANCES}"
fi



## PROCESSING ##

SUM_CORRECT=0
SUM_FAILED=0
SUM_AVG=0
FILES=$(find ${DIR} -type f -name "*.txt")

for FILE in ${FILES}
do
    # variables setup
    echo -e "\n–––––––––––––––––––––––––––––––––––––––––––"
    echo -e ">${FILE}\n"

    if [[ $(basename ${FILE}) == s_* ]]; then
        INTERVAL=$(( ${INSTANCES} + 1 ))
    else
        INTERVAL=$(( ${INSTANCES} / 5 ))  # $((${INSTANCES} / $((1 + $(echo ${FILE//[^0-9]/})))))
    fi

    CORRECT=0
    MISSED=0
    FAILED=0
    IS_DETECTED=true
    COUNTER=1
    TOTAL_DISTANCE=0
    AVERAGE=0

    while read LINE
    do
        if [[ $((${COUNTER} % ${INTERVAL})) -eq 0 ]]; then
            IS_DETECTED=false
            print_blue "DRIFT: ${COUNTER}"
        fi

        if [[ ${IS_DETECTED} = false && $((${COUNTER} % ${INTERVAL})) -eq ${TOLERATION} ]]; then
            ((MISSED++))
            print_yellow "MISSED: ${COUNTER}" 
            IS_DETECTED=true
        fi

      	if [[ "$LINE" == "D" && ${IS_DETECTED} = false ]]; then
    	    ((CORRECT++))
            IS_DETECTED=true
            ((TOTAL_DISTANCE+=$((${COUNTER} % ${INTERVAL}))))
            print_green "DETECTED: ${COUNTER}"
        elif [[ "$LINE" == "D" && ${IS_DETECTED} = true ]]; then
            ((FAILED++))
            print_red "FAILED: ${COUNTER}"
        fi
        
        ((COUNTER++))

    done < ${FILE}


    if [[ ${CORRECT} -eq 0 ]]; then
        AVERAGE="–"
    else
        AVERAGE=$((${TOTAL_DISTANCE} / ${CORRECT}))
    fi
    

    echo -e "\nCorrect=${CORRECT}"
    echo "Missed=${MISSED}"
    echo -e "Failed=${FAILED}"
    echo -e "Average distance=${AVERAGE}\n"

    ((SUM_CORRECT+=CORRECT))
    ((SUM_FAILED+=FAILED))
    if [[ "${AVERAGE}" != "–" ]]; then
        ((SUM_AVG+=AVERAGE))
    fi

    save_to_output "${FILE}" "${CORRECT}" "${MISSED}" "${FAILED}" "${AVERAGE}"

done


## SAVE ##

if [[ ! -z "${OUTPUT_DIR:-}" ]]; then
    (tail -n +1 ${OUTPUT_DIR} | sed 's/_/\\_/g' | sort --version-sort) > "${OUTPUT_DIR%.txt}.csv"
    rm -f ${OUTPUT_DIR}
fi

SUM_DIR="sum_${DIR}.csv"
echo "correct,failed,average" > ${SUM_DIR}
echo "${SUM_CORRECT},${SUM_FAILED},$(( ${SUM_AVG} / $(find  ${DIR} -type f -name "*.txt" -printf '.' | wc -c) ))" >> ${SUM_DIR}