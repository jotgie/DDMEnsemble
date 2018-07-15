#!/bin/bash


function print_red () {
    echo -e "\033[0;31m${*}\033[0m" >&2
}

function print_help () {
    echo "This script allows to sum up skrypcior results"
    echo -e "-f FILE\n\tCSV file with algorithm results"
    echo -e "-h\n\tPrints this help message"
}

function sum_files () {
    local -r ROWS=(${@})
    local -r OUTPUT=${4}
    local FILES=()
    local CORRECT=()
    local FAILED=()
    local MISSED=()
    local AVERAGE=()

    echo "correct,failed,missed,average" > "${DIRECTORY}/${OUTPUT}"

    for i in {0..2}; do
        local filename=$( echo ${ROWS[${i}]%%,*} | sed 's/\\_/_/g' )
        FILES+=( $filename )
        CORRECT+=( $(cut -d ',' -f2 <<< ${ROWS[${i}]} ) )
        FAILED+=( $(cut -d ',' -f3 <<< ${ROWS[${i}]} ) )
        MISSED+=( $(cut -d ',' -f4 <<< ${ROWS[${i}]} ) )
        AVERAGE+=( $(cut -d ',' -f5 <<< ${ROWS[${i}]} ) )
    done

    echo "Files: ${FILES[@]}"
    echo "Correct: ${CORRECT[@]}"
    echo "Failed: ${FAILED[@]}"
    echo "Missed: ${MISSED[@]}"
    echo "Average: ${AVERAGE[@]}"
    
    echo "$(sum_values_to_file "${CORRECT[@]}"),\
    $(sum_values_to_file "${FAILED[@]}"),\
    $(sum_values_to_file "${MISSED[@]}"),\
    $(sum_values_to_file "${AVERAGE[@]}")" >> "${DIRECTORY}/${OUTPUT}"    
}

function sum_values_to_file () {
    local -r COLUMN=(${@})
    local -r regex='^[0-9]+$'
    local total=0

    for i in {0..2}; do
        if [[ ${COLUMN[${i}]} =~ ${regex} ]]; then
            (( total+=${COLUMN[${i}]} ))
        fi
    done
    
    echo "${total}"
}


while getopts ":hf:" OPT; do
  case ${OPT} in
    h)
        print_help
        exit 0
        ;;
    f)
        FILE=${OPTARG}
        ;;
    \?)
        print_red "ERROR: Invalid option: -${OPTARG}"
        exit 1
        ;;
  esac
done


## VALIDATION ##
if [[ -z "${FILE:-}" ]]; then
    print_red "You need to provide a file with -f option!"
    exit 1
elif [[ ! -f "${FILE}" ]]; then
    print_red "Provided value is not a file!"
    exit 1
fi

DIRECTORY="sum"

if [[ ! -d "${DIRECTORY}" ]]; then
  mkdir ${DIRECTORY}
fi


## PROCESSING ##
IFS=$'\n'

ID_HYP_HYP=($( egrep '^id\\_(s\\_hyp\\_r[1-3](\\_)?){2}' ${FILE} ))
ID_LED_LED=($( egrep '^id\\_(s\\_led\\_r[1-3](\\_)?){2}' ${FILE} ))
ID_RBF_RBF=($( egrep '^id\\_(s\\_rbf\\_r[1-3](\\_)?){2}' ${FILE} ))
ID_HYP_RBF=($( egrep '^id\\_s\\_hyp\\_r[1-3]\\_s\\_rbf\\_r[1-3]' ${FILE} ))
SD_HYP_HYP=($( egrep '^sd\\_(s\\_hyp\\_r[1-3](\\_)?){2}' ${FILE} ))
SD_LED_LED=($( egrep '^sd\\_(s\\_led\\_r[1-3](\\_)?){2}' ${FILE} ))
SD_RBF_RBF=($( egrep '^sd\\_(s\\_rbf\\_r[1-3](\\_)?){2}' ${FILE} ))
SD_HYP_RBF=($( egrep '^sd\\_s\\_hyp\\_r[1-3]\\_s\\_rbf\\_r[1-3]' ${FILE} ))
S_HYP=($( egrep '^s\\_hyp' ${FILE} ))
S_LED=($( egrep '^s\\_led' ${FILE} ))
S_RBF=($( egrep '^s\\_rbf' ${FILE} ))

sum_files "${ID_HYP_HYP[@]}" "id_hyp_hyp.csv"
sum_files "${ID_LED_LED[@]}" "id_led_led.csv"
sum_files "${ID_RBF_RBF[@]}" "id_rbf_rbf.csv"
sum_files "${ID_HYP_RBF[@]}" "id_hyp_rbf.csv"
sum_files "${SD_HYP_HYP[@]}" "sd_hyp_hyp.csv"
sum_files "${SD_LED_LED[@]}" "sd_led_led.csv"
sum_files "${SD_RBF_RBF[@]}" "sd_rbf_rbf.csv"
sum_files "${SD_HYP_RBF[@]}" "sd_hyp_rbf.csv"
sum_files "${S_HYP[@]}" "s_hyp§.csv"
sum_files "${S_LED[@]}" "s_led.csv"
sum_files "${S_RBF[@]}" "s_rbf.csv"

